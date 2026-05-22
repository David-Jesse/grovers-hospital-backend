package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.TestComponent;
import com.djio.grover_hospital.model.enums.TestComponentFlag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestComponentResponse {

    private Long id;
    private Long resultId;
    private String name;
    private String value;
    private String unit;
    private String referenceRange;
    private TestComponentFlag flag;
    private String notes;
    private Integer displayOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static TestComponentResponse from(TestComponent c) {
        return TestComponentResponse.builder()
                .id(c.getId())
                .resultId(c.getResult() != null ? c.getResult().getId() : null)
                .name(c.getName())
                .value(c.getValue())
                .unit(c.getUnit())
                .referenceRange(c.getReferenceRange())
                .flag(c.getFlag())
                .notes(c.getNotes())
                .displayOrder(c.getDisplayOrder())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}