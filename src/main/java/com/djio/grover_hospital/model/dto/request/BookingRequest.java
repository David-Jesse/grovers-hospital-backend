package com.djio.grover_hospital.model.dto.request;


import com.djio.grover_hospital.model.enums.BookingType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    @NotNull(message = "Booking type is required")
    private BookingType bookingType;

    /** Required when bookingType = CONSULTATION */
    private Long departmentId;

    /** Required when bookingType = PACKAGE */
    private Long packageId;

    /** Optional even for PACKAGE — patient may want guidance on tier */
    private Long packageTierId;

    @NotNull(message = "Preferred date is required")
    @Future(message = "Preferred data must be in the future")
    private LocalDate preferredDate;

    /** Optional notes from patient (symptoms, special requests, etc.) */
    private String notes;
}
