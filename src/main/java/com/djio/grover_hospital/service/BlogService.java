package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.BlogPostRequest;
import com.djio.grover_hospital.model.dto.response.BlogPostResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.entity.BlogPost;
import com.djio.grover_hospital.repository.BlogPostRepository;
import com.djio.grover_hospital.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {

    private final BlogPostRepository blogPostRepository;


    // === Public read ===
    public PageResponse<BlogPostResponse> getPublishedPosts(Pageable pageable, String tag) {
        Page<BlogPost> page = (tag == null || tag.isBlank())
                ? blogPostRepository.findByIsPublishedTrueOrderByPublishedAtDesc(pageable)
                : blogPostRepository.findByIsPublishedTrueAndTagsContainingIgnoreCaseOrderByPublishedAtDesc(tag, pageable);

        return PageResponse.from(page, BlogPostResponse::fromList);
    }

    public BlogPostResponse getPublishedPostsBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "slug", slug));

        return BlogPostResponse.fromDetail(post);
    }

    // == Admin ==
    public PageResponse<BlogPostResponse> getAllForAdmin(Pageable pageable) {
        Pageable sortedByCreated = pageable.getSort().isSorted() ? pageable :
                org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
        Page<BlogPost> page = blogPostRepository.findAll(sortedByCreated);
        return PageResponse.from(page, BlogPostResponse::fromList);
    }

    public BlogPostResponse getByIdForAdmin(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));
        return BlogPostResponse.fromDetail(post);
    }

    @Transactional
    public BlogPostResponse create(BlogPostRequest request) {
        String slug = generateUniqueSlug(request.getTitle(), null);
        boolean publishing = Boolean.TRUE.equals(request.getIsPublished());

        BlogPost post = BlogPost.builder()
                .title(request.getTitle())
                .slug(slug)
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .featuredImage(request.getFeaturedImage())
                .tags(request.getTags())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .isPublished(request.getIsPublished())
                .publishedAt(publishing ? OffsetDateTime.now() : null)
                .build();

        return BlogPostResponse.fromDetail(blogPostRepository.save(post));
    }

    @Transactional
    public BlogPostResponse update(Long id, BlogPostRequest request) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));

        if (!post.getTitle().equals(request.getTitle())) {
            post.setSlug(generateUniqueSlug(request.getTitle(), id));
        }

        post.setTitle(request.getTitle());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setFeaturedImage(request.getFeaturedImage());
        post.setTags(request.getTags());
        post.setMetaTitle(request.getMetaTitle());
        post.setMetaDescription(request.getMetaDescription());

        // Handle publish state transitions
        if (request.getIsPublished() != null) {
            boolean wasPublished = Boolean.TRUE.equals(post.getIsPublished());
            boolean willBePublished = Boolean.TRUE.equals(request.getIsPublished());
            post.setIsPublished(willBePublished);

            // Set publishedAt only on first publish; preserve original data on subsequent edits

            if (!wasPublished && willBePublished) {
                post.setPublishedAt(OffsetDateTime.now());
            }
        }

        return BlogPostResponse.fromDetail(blogPostRepository.save(post));
    }

    @Transactional
    public BlogPostResponse togglePublish(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post", "id", id));

        boolean nowPublishing = !Boolean.TRUE.equals(post.getIsPublished());
        post.setIsPublished(nowPublishing);
        if (nowPublishing && post.getPublishedAt() == null) {
            post.setPublishedAt(OffsetDateTime.now());
        }

        return BlogPostResponse.fromDetail(blogPostRepository.save(post));
    }

    @Transactional
    public void delete(Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog post", "id", id);
        }

        blogPostRepository.deleteById(id);
    }

    // === Helper ===

    private String generateUniqueSlug(String title, Long excludeId) {
        String baseSlug = SlugUtils.toSlug(title);
        if (baseSlug.isEmpty()) {
            throw new BadRequestException("Title produces an invalid slug");
        }

        String slug = baseSlug;
        int counter = 1;
        while (slugExists(slug, excludeId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private boolean slugExists(String slug, Long excludeId) {
        return blogPostRepository.findBySlug(slug)
                .filter(p -> !p.getId().equals(excludeId))
                .isPresent();
    }

    /**
     * Cleans up tag input - trims whitespaces, lowercases, removes empty tags.
     */

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) return null;
        return java.util.Arrays.stream(tags.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(","));
    }
}