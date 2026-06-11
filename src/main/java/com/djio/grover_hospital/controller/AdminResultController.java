package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.CreateResultRequest;
import com.djio.grover_hospital.model.dto.response.AdminResultResponse;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DecryptedFileStream;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.service.ResultService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/results")
@RequiredArgsConstructor
public class AdminResultController {

    private final ResultService resultService;

    /**
     * Upload a new result with one or more files
     * Multipart form-data with parts
     * - "metadata" -> JSON CreateResultRequest
     * - "files"    -> one or more files (PDF/JPEG/PNG, 10mb each max)
     */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminResultResponse>> uploadResult(
            @Valid @RequestPart("metadata") CreateResultRequest request,
            @RequestPart("files") List<MultipartFile> files,
            HttpServletRequest httpRequest
    ) {
        AdminResultResponse response = resultService.uploadResult(request, files, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Result uploaded", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminResultResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(resultService.getAllForAdmin(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminResultResponse>> getById(
            @PathVariable Long id, HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(resultService.getByIdForAdmin(id, httpRequest)));
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity<ApiResponse<AdminResultResponse>> notifyPatient(
            @PathVariable Long id, HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success("Patient notified",
                resultService.notifyPatient(id, httpRequest)
        ));
    }

    @GetMapping("/{resultId}/files/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable Long resultId,
            @PathVariable Long fileId,
            HttpServletRequest httpRequest
    ) {
        DecryptedFileStream stream = resultService.downloadFileForAdmin(resultId, fileId, httpRequest);
        return buildFileResponse(stream);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id, HttpServletRequest httpRequest
    ) {
        resultService.deleteResult(id, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Result deleted", null));
    }

    private ResponseEntity<InputStreamResource> buildFileResponse(DecryptedFileStream stream) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename(stream.getOriginalFileName())
                        .build());
        headers.setCacheControl("no-store");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(stream.getContentType()))
                .body(new InputStreamResource(stream.getStream()));
    }

    @RestController
    @RequestMapping("/admin/patients")
    @RequiredArgsConstructor
    public class AdminPatientResultController {

        private final ResultService resultService;

        @GetMapping("/{patientId}/results")
        public ResponseEntity<ApiResponse<PageResponse<AdminResultResponse>>> listForPatient(
                @PathVariable Long patientId,
                @PageableDefault(size = 20) Pageable pageable
        ) {
            return ResponseEntity.ok(ApiResponse.success(
                    resultService.getResultsForPatient(patientId, pageable)));
        }
    }
}