package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.response.BlogPostResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.entity.BlogPost;
import com.djio.grover_hospital.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {

    private final BlogPostRepository blogPostRepository;

    public PageResponse<BlogPostResponse> getPublishedPosts(Pageable pageable, String tag) {
        Page<BlogPost> page = (tag == null || tag.isBlank())
                ? blogPostRepository.findByIsPublishedTrueOrderByPublishedAtDesc(pageable)
                : blogPostRepository.findByIsPublishedTrueAndTagsContainingIgnoreCaseOrderByPublishedAtDesc(tag, pageable);

        return PageResponse.from(page, BlogPostResponse::fromList);
    }

    public BlogPostResponse getPublishedPostsBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "slug", slug));

        return BlogPostResponse.fromDetails(post);
    }
}