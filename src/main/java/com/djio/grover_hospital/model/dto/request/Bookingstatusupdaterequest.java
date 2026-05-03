package com.djio.grover_hospital.model.dto.request;

import com.djio.grover_hospital.model.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Bookingstatusupdaterequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;

    /** Optional internal note attached to the status change */
    private String adminNotes;
}