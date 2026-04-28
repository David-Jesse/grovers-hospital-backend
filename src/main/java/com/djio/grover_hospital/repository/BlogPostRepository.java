package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlug(String slug);

    Optional<BlogPost> findBySlugAndIsPublishedTrue(String slug);

    Page<BlogPost> findByIsPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    Page<BlogPost> findByIsPublishedTrueAndTagsContainingIgnoreCaseOrderByPublishedAtDesc(String tag, Pageable pageable);

    boolean existsBySlug(String slug);
}