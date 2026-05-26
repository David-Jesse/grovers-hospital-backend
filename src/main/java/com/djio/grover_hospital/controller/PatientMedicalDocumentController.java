package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.MedicalDocumentResponse;
import com.djio.grover_hospital.model.enums.DocumentCategory;
import com.djio.grover_hospital.service.MedicalDocumentService;
import com.djio.grover_hospital.service.MedicalDocumentService.DocumentDownload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/portal/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patient Portal - Medical Documents",
        description = "Upload, view, download, and delete your own medical documents (insurance, referrals, external reports). Encrypted at rest.")
public class PatientMedicalDocumentController {

    private final MedicalDocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a medical document",
            description = "Multipart upload. PDF/JPEG/PNG, max 10MB. Encrypted before storage.")
    public ResponseEntity<ApiResponse<MedicalDocumentResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "OTHER") DocumentCategory category,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest httpRequest) {
        MedicalDocumentResponse doc = documentService.uploadAsPatient(file, category, title, description, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded", doc));
    }

    @GetMapping
    @Operation(summary = "List my documents",
            description = "Optional ?category= filter. Returns metadata only; use the download endpoint for file contents.")
    public ResponseEntity<ApiResponse<List<MedicalDocumentResponse>>> listMine(
            @RequestParam(value = "category", required = false) DocumentCategory category) {
        List<MedicalDocumentResponse> docs = documentService.getMyDocuments(category);
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved", docs));
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download one of my documents",
            description = "Decrypts and streams the file. Ownership enforced.")
    public ResponseEntity<Resource> download(@PathVariable Long documentId) {
        DocumentDownload dl = documentService.downloadAsPatient(documentId);
        return buildDownloadResponse(dl);
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete one of my own uploaded documents",
            description = "Patients can only delete documents they uploaded themselves, not admin-placed ones.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long documentId,
                                                    HttpServletRequest httpRequest) {
        documentService.deleteAsPatient(documentId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }

    private ResponseEntity<Resource> buildDownloadResponse(DocumentDownload dl) {
        ByteArrayResource resource = new ByteArrayResource(dl.data());
        MediaType mediaType = dl.contentType() != null
                ? MediaType.parseMediaType(dl.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + dl.fileName() + "\"")
                .contentLength(dl.data().length)
                .body(resource);
    }
}