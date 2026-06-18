package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.DecryptedFileStream;
import com.djio.grover_hospital.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/results")
@RequiredArgsConstructor
@Tag(name = "Public - Result Downloads",
        description = "Token-authenticated downloads sent via email. No login required — the token IS the auth.")
public class PublicResultController {

    private final ResultService resultsService;

    @GetMapping("/download")
    @Operation(summary = "Download a result file using a signed token from the email link")
    public ResponseEntity<Resource> download(
            @RequestParam("token") String token,
            HttpServletRequest httpRequest) {

        DecryptedFileStream stream = resultsService.downloadByToken(token, httpRequest);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(stream.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + stream.getOriginalFileName() + "\"")
                .body(new InputStreamResource(stream.getStream()));
    }
}