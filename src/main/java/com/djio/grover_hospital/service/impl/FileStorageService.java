package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.FileStorageException;
import com.djio.grover_hospital.model.dto.response.ImageUploadResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final Path uploadRoot;
    private final String publicBaseUrl;

    public FileStorageService(
            @Value("${app.file-storage.upload-dir:./uploads/images}") String uploadDir,
            @Value("${app.file-storage.public-base-url:/files}") String publicBaseUrl) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadRoot);
            log.info("Image upload directory: {}", uploadRoot);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory: " + uploadRoot, ex);
        }
    }

    public ImageUploadResponse uploadImage(MultipartFile file) {
        validate(file);

        String extension = getExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "." + extension;
        Path targetPath = uploadRoot.resolve(storedFileName);

        // Prevent path traversal
        if (!targetPath.getParent().equals(uploadRoot)) {
            throw new BadRequestException("Invalid file name");
        }

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file: " + storedFileName, ex);
        }

        return ImageUploadResponse.builder()
                .url(publicBaseUrl + storedFileName)
                .fileName(storedFileName)
                .size(file.getSize())
                .build();
    }

    public Path resolveFilePath(String fileName) {
        Path file = uploadRoot.resolve(fileName).normalize();
        if (!file.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid file path");
        }
        return file;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPEG, PNG, WEBP and GIF images are allowed");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file extension: " + extension);
        }
    }

    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BadRequestException("File must have an extension");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}