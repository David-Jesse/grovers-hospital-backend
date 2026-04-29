package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.BlogPostResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BlogPostResponse>>> getPublishedPosts(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPublishedPosts(pageable, tag)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BlogPostResponse>> getPostsBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPublishedPostsBySlug(slug)));
    }
}