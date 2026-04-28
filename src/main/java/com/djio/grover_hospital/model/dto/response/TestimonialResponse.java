package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.Testimonial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialResponse {

    private Long id;
    private String patientName;
    private String content;
    private Integer rating;

    public static TestimonialResponse from(Testimonial testimonial) {
        return TestimonialResponse.builder()
                .id(testimonial.getId())
                .patientName(testimonial.getPatientName())
                .content(testimonial.getContent())
                .rating(testimonial.getRating())
                .build();
    }
}