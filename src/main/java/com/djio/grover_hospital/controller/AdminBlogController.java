package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.BlogPostRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.BlogPostResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.entity.BlogPost;
import com.djio.grover_hospital.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BlogPostResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getAllForAdmin(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getByIdForAdmin(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BlogPostResponse>> create(@Valid @RequestBody BlogPostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog post created", blogService.create(request)));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<BlogPostResponse>> togglePublish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Publish state toggled", blogService.togglePublish(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Blog post updated", blogService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        blogService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Blog post deleted", null));
    }
}
