package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.BlogPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostResponse {

    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    /**
     * Full HTML/markdown content — only populated for the detail view, not the list view.
     */
    private String content;
    private String featuredImage;
    private List<String> tags;
    private String metaTitle;
    private String category;
    private String metaDescription;
    private OffsetDateTime publishedAt;

    public static BlogPostResponse fromList(BlogPost post) {
        return baseBuilder(post).build();
    }

    public static BlogPostResponse fromDetail(BlogPost post) {
        return baseBuilder(post)
                .content(post.getContent())
                .build();
    }

    private static BlogPostResponseBuilder baseBuilder(BlogPost post) {
        return BlogPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .featuredImage(post.getFeaturedImage())
                .tags(parseTags(post.getTags()))
                .category(post.getCategory())
                .metaTitle(post.getMetaTitle())
                .metaDescription(post.getMetaDescription())
                .publishedAt(post.getPublishedAt());
    }

    private static List<String> parseTags(String tagsString) {
        if (tagsString == null || tagsString.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
