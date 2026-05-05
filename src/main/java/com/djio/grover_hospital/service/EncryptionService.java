package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM envelope encryption for patient result files.

 * Architecture:
 *   - Master key: from app.encryption.master-key env variable (NEVER in DB or code)
 *   - Each file gets a unique data encryption key (DEK)
 *   - File is encrypted with its DEK
 *   - DEK is encrypted with master key, stored alongside the file metadata
 *
 * Why GCM instead of CBC: GCM provides authenticated encryption, so any
 * tampering with the ciphertext is detected on decryption.
 *
 * Storage format on disk: [12-byte IV][ciphertext + 16-byte auth tag]
 * Encrypted DEK format:    base64([12-byte IV][encrypted DEK + 16-byte auth tag])
 */
@Service
@Slf4j
public class EncryptionService {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_SIZE_BITS = 256;
    private static final int GCM_IV_LENGTH = 12;     // 96 bits — recommended for GCM
    private static final int GCM_TAG_LENGTH = 128;   // 128 bits auth tag
    private static final int BUFFER_SIZE = 8192;

    private final SecretKey masterKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(@Value("${app.encryption.master-key}") String masterKeyBase64) {
        if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
            throw new IllegalStateException(
                    "app.encryption.master-key is not configured. Generate one with: " +
                            "openssl rand -base64 32");
        }
        byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "Master key must be exactly 32 bytes (256 bits) when base64-decoded. " +
                            "Got " + keyBytes.length + " bytes.");
        }
        this.masterKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    @PostConstruct
    public void init() {
        log.info("EncryptionService initialized with AES-256-GCM");
    }

    /** Generates a new random data encryption key for one file. */
    public SecretKey generateDataKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGen.init(KEY_SIZE_BITS, secureRandom);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new FileStorageException("Failed to generate data key", e);
        }
    }

    /** Encrypts a DEK with the master key. Returns base64 of [IV || ciphertext || tag]. */
    public String encryptDataKey(SecretKey dataKey) {
        try {
            byte[] iv = randomIv();
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(dataKey.getEncoded());

            ByteBuffer buf = ByteBuffer.allocate(iv.length + encrypted.length);
            buf.put(iv);
            buf.put(encrypted);
            return Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new FileStorageException("Failed to encrypt data key", e);
        }
    }

    /** Decrypts an envelope-encrypted DEK back into a usable SecretKey. */
    public SecretKey decryptDataKey(String encryptedDekBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedDekBase64);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] dekBytes = cipher.doFinal(ciphertext);

            return new SecretKeySpec(dekBytes, KEY_ALGORITHM);
        } catch (Exception e) {
            throw new FileStorageException("Failed to decrypt data key — file may be corrupted or master key is wrong", e);
        }
    }

    /**
     * Encrypts a stream of plaintext into an output stream.
     * Writes [12-byte IV][ciphertext+tag] to the output.
     * Returns total bytes written.
     */
    public long encryptStream(InputStream plaintext, OutputStream encryptedOutput, SecretKey dataKey) {
        try {
            byte[] iv = randomIv();
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, dataKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            // Write the IV first so we can recover it on decryption
            encryptedOutput.write(iv);
            long totalBytes = iv.length;

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = plaintext.read(buffer)) != -1) {
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) {
                    encryptedOutput.write(encrypted);
                    totalBytes += encrypted.length;
                }
            }

            byte[] finalBlock = cipher.doFinal();
            encryptedOutput.write(finalBlock);
            totalBytes += finalBlock.length;

            return totalBytes;
        } catch (Exception e) {
            throw new FileStorageException("Failed to encrypt file stream", e);
        }
    }

    /**
     * Decrypts an encrypted file stream into an output stream.
     * Expects the input to start with the 12-byte IV, followed by ciphertext+tag.
     */
    public void decryptStream(InputStream encryptedInput, OutputStream plaintextOutput, SecretKey dataKey) {
        try {
            // Read the IV from the start of the stream
            byte[] iv = new byte[GCM_IV_LENGTH];
            int ivBytesRead = encryptedInput.read(iv);
            if (ivBytesRead != GCM_IV_LENGTH) {
                throw new FileStorageException("Encrypted file is corrupted (missing IV)");
            }

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, dataKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = encryptedInput.read(buffer)) != -1) {
                byte[] decrypted = cipher.update(buffer, 0, bytesRead);
                if (decrypted != null) {
                    plaintextOutput.write(decrypted);
                }
            }

            byte[] finalBlock = cipher.doFinal();
            plaintextOutput.write(finalBlock);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read encrypted file", e);
        } catch (Exception e) {
            throw new FileStorageException("Failed to decrypt file — file may be tampered with or wrong key", e);
        }
    }

    private byte[] randomIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }
}