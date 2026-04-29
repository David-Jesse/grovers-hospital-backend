package com.djio.grover_hospital.service;


import com.djio.grover_hospital.model.dto.response.TestimonialResponse;
import com.djio.grover_hospital.repository.TestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    public List<TestimonialResponse> getAllApproved() {
        return testimonialRepository.findByIsApprovedTrueOrderByDisplayOrderAsc()
                .stream()
                .map(TestimonialResponse::from)
                .toList();
    }
}