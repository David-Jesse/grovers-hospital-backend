package com.djio.grover_hospital.model.dto.request;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogPostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500)
    private String title;

    @Size(max = 1000)
    private String excerpt;

    @Size(max = 100)
    private String category;

    @NotBlank(message = "Content is required")
    private String content;

    @Column(name = "featured_image", length = 500)
    private String featuredImage;


    @Size(max = 500)
    private String tags;

    @Size(max = 200)
    private String metaTitle;

    @Size(max = 500)
    private String metaDescription;

    private Boolean isPublished;
}