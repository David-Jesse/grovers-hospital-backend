package com.djio.grover_hospital.model.dto.response;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.model.enums.BookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * Patient-facing booking view.
 * Does NOT include adminNotes — those are internal-only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private BookingType bookingType;
    private BookingStatus status;
    private LocalDate preferredDate;

    // Consultation target
    private Long departmentId;
    private String departmentName;

    // Package target
    private Long packageId;
    private String packageName;
    private Long packageTierId;
    private String packageTierName;

    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private LocalTime appointmentTime;

    private Integer rescheduleCount;
    private OffsetDateTime lastRescheduledAt;

    public static BookingResponse from(Booking booking) {
        BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .bookingType(booking.getBookingType())
                .status(booking.getStatus())
                .preferredDate(booking.getPreferredDate())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .appointmentTime(booking.getAppointmentTime())
                .lastRescheduledAt(booking.getLastRescheduledAt())
                .rescheduleCount(booking.getRescheduleCount());

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