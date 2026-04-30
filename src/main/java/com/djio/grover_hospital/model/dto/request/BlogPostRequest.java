package com.djio.grover_hospital.model.dto.request;


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

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500)
    private String tags;

    @Size(max = 200)
    private String metaTitle;

    @Size(max = 500)
    private String metaDescription;

    private Boolean isPublished;
}