package com.djio.grover_hospital.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "testimonials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_name", nullable = false, length = 150)
    private String patientName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Integer rating;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}