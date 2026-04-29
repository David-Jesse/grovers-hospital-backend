package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.TestimonialResponse;
import com.djio.grover_hospital.service.TestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialService testimonialService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TestimonialResponse>>> getApprovedTestimonials() {
        return ResponseEntity.ok(ApiResponse.success(testimonialService.getAllApproved()));
    }
}
