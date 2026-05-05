package com.djio.grover_hospital.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

/**
 * Used by the controller to stream a decrypted file to the patient
 * without writing the plaintext to disk
 */
@Data
@Builder
@AllArgsConstructor
public class DecryptedFileStream {

    private String originalFileName;
    private String contentType;
    private long contentLength;
    private InputStream stream;
}