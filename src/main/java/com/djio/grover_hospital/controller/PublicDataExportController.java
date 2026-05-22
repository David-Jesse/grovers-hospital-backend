package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.service.DataExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/account/export-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public - Data Export Download",
        description = "Token-authenticated download. Link is sent to the patient's email.")
public class PublicDataExportController {

    private final DataExportService dataExportService;

    @GetMapping("/{token}")
    @Operation(summary = "Download an exported data file by token",
            description = "Returns the JSON file as an attachment. No JWT required; " +
                    "the token in the path is the authentication.")
    public ResponseEntity<Resource> download(@PathVariable String token) {
        DataExportService.ExportFileHandle handle = dataExportService.resolveForDownload(token);

        Resource fileResource = new FileSystemResource(handle.path());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + handle.filename() + "\""
                )
                .body(fileResource);
    }
}
