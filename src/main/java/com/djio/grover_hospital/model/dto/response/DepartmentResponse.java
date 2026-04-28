package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private Integer displayOrder;

    public static DepartmentResponse from(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .slug(department.getSlug())
                .description(department.getDescription())
                .iconUrl(department.getIconUrl())
                .displayOrder(department.getDisplayOrder())
                .build();
    }
}
