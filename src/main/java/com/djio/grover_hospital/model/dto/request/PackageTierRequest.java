package com.djio.grover_hospital.model.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PackageTierRequest {

    @NotBlank(message = "Tier name is required")
    @Size(max = 100)
    private String name;

    private String inclusions;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal priceMale;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal priceFemale;

    @Size(max = 500)
    private String notes;

    private Integer displayOrder;

    private Boolean isActive;
}
