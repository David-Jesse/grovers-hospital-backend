package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DecryptedFileStream;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.dto.response.ResultResponse;
import com.djio.grover_hospital.service.ResultService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/results")
@RequiredArgsConstructor
public class PatientResultController {

    private final ResultService resultService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ResultResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(resultService.getMyResults(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResultResponse>> getById(
            @PathVariable Long id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.success(resultService.getMyResultById(id, httpRequest)));
    }

    @GetMapping("/{resultId}/files/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable Long resultId,
            @PathVariable Long fileId,
            HttpServletRequest httpRequest
    ) {
        DecryptedFileStream stream = resultService.downloadFileForPatient(resultId, fileId, httpRequest);

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
}