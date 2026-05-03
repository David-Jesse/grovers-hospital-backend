package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.model.enums.BookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Admin-facing booking view — includes full patient contact details
 * and adminNotes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingResponse {

    private Long id;
    private BookingType bookingType;
    private BookingStatus status;
    private LocalDate preferredDate;

    // Patient details
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String patientEmail;
    private String patientPhone;

    // Consultation target
    private Long departmentId;
    private String departmentName;

    // Package target
    private Long packageId;
    private String packageName;
    private Long packageTierId;
    private String packageTierName;

    private String notes;
    private String adminNotes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AdminBookingResponse from(Booking booking) {
        AdminBookingResponseBuilder builder = AdminBookingResponse.builder()
                .id(booking.getId())
                .bookingType(booking.getBookingType())
                .status(booking.getStatus())
                .preferredDate(booking.getPreferredDate())
                .patientId(booking.getPatient().getId())
                .patientFirstName(booking.getPatient().getFirstName())
                .patientLastName(booking.getPatient().getLastName())
                .patientEmail(booking.getPatient().getEmail())
                .patientPhone(booking.getPatient().getPhone())
                .notes(booking.getNotes())
                .adminNotes(booking.getAdminNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        if (booking.getDepartment() != null) {
            builder.departmentId(booking.getDepartment().getId())
                    .departmentName(booking.getDepartment().getName());
        }
        if (booking.getHealthPackage() != null) {
            builder.packageId(booking.getHealthPackage().getId())
                    .packageName(booking.getHealthPackage().getName());
        }
        if (booking.getPackageTier() != null) {
            builder.packageTierId(booking.getPackageTier().getId())
                    .packageTierName(booking.getPackageTier().getName());
        }

        return builder.build();
    }
}