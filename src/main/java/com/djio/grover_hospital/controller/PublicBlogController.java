package com.djio.grover_hospital.controller;

import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.BlogPostResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/blog")
@RequiredArgsConstructor
public class PublicBlogController {

    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BlogPostResponse>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getAllPublished(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPublishedById(id)));
    }
}