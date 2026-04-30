package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.TestimonialRequest;
import com.djio.grover_hospital.model.dto.response.TestimonialResponse;
import com.djio.grover_hospital.model.entity.Testimonial;
import com.djio.grover_hospital.repository.TestimonialRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    // === Public read ==

    public List<TestimonialResponse> getAllApproved() {
        return testimonialRepository.findByIsApprovedTrueOrderByDisplayOrderAsc()
                .stream()
                .map(TestimonialResponse::from)
                .toList();
    }

    // === Admin ===

    public List<TestimonialResponse> getAllForAdmin() {
        return testimonialRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(TestimonialResponse::from)
                .toList();
    }

    @Transactional
    public TestimonialResponse create(TestimonialRequest request) {
        Testimonial testimonial = Testimonial.builder()
                .patientName(request.getPatientName())
                .content(request.getContent())
                .rating(request.getRating())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isApproved(request.getIsApproved() != null ? request.getIsApproved() : false)
                .build();

        return TestimonialResponse.from(testimonialRepository.save(testimonial));
    }

    @Transactional
    public TestimonialResponse update(Long id, @Valid TestimonialRequest request) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial", "id", id));

        testimonial.setPatientName(request.getPatientName());
        testimonial.setContent(request.getContent());
        testimonial.setRating(request.getRating());
        if (request.getDisplayOrder() != null) testimonial.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsApproved() != null) testimonial.setIsApproved(request.getIsApproved());

        return TestimonialResponse.from(testimonialRepository.save(testimonial));
    }

    @Transactional
    public TestimonialResponse toggleApproval(Long id) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial", "id", id));

        testimonial.setIsApproved(!Boolean.TRUE.equals(testimonial.getIsApproved()));

        return TestimonialResponse.from(testimonialRepository.save(testimonial));
    }

    @Transactional
    public void delete(Long id) {
        if (!testimonialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Testimonial", "id", id);
        }
        testimonialRepository.deleteById(id);
    }
}