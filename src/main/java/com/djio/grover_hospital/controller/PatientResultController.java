package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.DecryptedFileStream;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.dto.response.ResultResponse;
import com.djio.grover_hospital.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{resultId}/email-link")
    @Operation(summary = "Email a secure download link for the result",
            description = "Sends a 30-minute signed download link to your registered email. " +
                    "If the result has multiple files, the first file's link is sent.")
    public ResponseEntity<ApiResponse<Void>> requestDownloadLink(
            @PathVariable Long resultId,
            HttpServletRequest httpRequest) {
        resultService.requestResultDownloadLink(resultId, httpRequest);
        return ResponseEntity.accepted()
                .body(ApiResponse.success(
                        "A download link has been sent to your email. It expires in 30 minutes.",
                        null));
    }
}