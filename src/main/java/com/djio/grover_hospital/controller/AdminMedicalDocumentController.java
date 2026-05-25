package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.MedicalDocumentResponse;
import com.djio.grover_hospital.model.enums.DocumentCategory;
import com.djio.grover_hospital.service.MedicalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Medical Documents",
        description = "Admin upload/list/download/delete of patient medical documents. Encrypted at rest.")
public class AdminMedicalDocumentController {

    private final MedicalDocumentService documentService;

    @PostMapping(value = "/patients/{patientId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document for a patient")
    public ResponseEntity<ApiResponse<MedicalDocumentResponse>> upload(
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "OTHER") DocumentCategory category,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest httpRequest
    ) {
        MedicalDocumentResponse doc = documentService.uploadAsAdmin(patientId, file, category, title, description, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded", doc));
    }

    @GetMapping("/patients/{patientId}/documents")
    @Operation(summary = "List a patient's documents")
    public ResponseEntity<ApiResponse<List<MedicalDocumentResponse>>> listForPatient(@PathVariable Long patientId) {
        List<MedicalDocumentResponse> docs = documentService.getDocumentsForPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved", docs));
    }

    @GetMapping("/documents/{documentId}/download")
    @Operation(summary = "Downliad a patient's document")
    public ResponseEntity<Resource> download(@PathVariable Long documentId) {
        MedicalDocumentService.DocumentDownload dl = documentService.downloadAsAdmin(documentId);
        ByteArrayResource resource = new ByteArrayResource(dl.data());
        MediaType mediaType = dl.contentType() != null
                ? MediaType.parseMediaType(dl.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + dl.fileName() + "\""
                )
                .contentLength(dl.data().length)
                .body(resource);
    }

    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "Delete a document")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long documentId, HttpServletRequest httpRequest) {
        documentService.deleteAsAdmin(documentId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}
