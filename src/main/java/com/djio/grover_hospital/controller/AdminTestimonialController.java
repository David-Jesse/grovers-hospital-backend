package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.TestimonialRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.TestimonialResponse;
import com.djio.grover_hospital.service.TestimonialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/testimonials")
@RequiredArgsConstructor
public class AdminTestimonialController {

    private final TestimonialService testimonialService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TestimonialResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(testimonialService.getAllForAdmin()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TestimonialResponse>> create(@Valid @RequestBody TestimonialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Testimonial created", testimonialService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TestimonialResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TestimonialRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Testimonial updated", testimonialService.update(id, request)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TestimonialResponse>> toggleApproval(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Approval toggled", testimonialService.toggleApproval(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        testimonialService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Testimonial deleted", null));
    }
}
