package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.request.BookingRequest;
import com.djio.grover_hospital.model.dto.request.BookingStatusUpdateRequest;
import com.djio.grover_hospital.model.dto.response.AdminBookingResponse;
import com.djio.grover_hospital.model.dto.response.BookingResponse;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.entity.*;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.model.enums.BookingType;
import com.djio.grover_hospital.repository.*;
import com.djio.grover_hospital.security.SecurityUtils;
import com.djio.grover_hospital.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final HealthPackageRepository packageRepository;
    private final NotificationService notificationService;
    private final PackageTierRepository tierRepository;

    // ==== Patient operations ====

    @Transactional
    public BookingResponse createBookingForCurrentPatient(BookingRequest request) {
        Long patientId = SecurityUtils.getCurrentUserId();
        assert patientId != null;
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UnauthorizedException("Patient session is invalid"));

        validateBookingRequest(request);

        Booking booking = Booking.builder()
                .patient(patient)
                .bookingType(request.getBookingType())
                .preferredDate(request.getPreferredDate())
                .notes(request.getNotes())
                .status(BookingStatus.PENDING)
                .build();

        // Wire up the right targets based on booking type
        if (request.getBookingType() == BookingType.CONSULTATION) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            booking.setDepartment(department);
        } else {
            HealthPackage healthPackage = packageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Package", "id", request.getPackageTierId()));
            booking.setHealthPackage(healthPackage);

            if (request.getPackageId() != null) {
                PackageTier tier = tierRepository.findById(request.getPackageTierId())
                        .orElseThrow(() -> new ResourceNotFoundException("Package tier", "id", request.getPackageTierId()));

                if (!tier.getHealthPackage().getId().equals(healthPackage.getId())) {
                    throw new BadRequestException("Selected tier does not belong to the chosen package");
                }
                booking.setPackageTier(tier);
            }
        }

        Booking saved = bookingRepository.save(booking);
        log.info("Booking #{} created by patient {}", saved.getId(), patient.getId());

        // Fire notifications
        notificationService.notifyBookingConfirmationToPatient(saved);
        notificationService.notifyBookingAlertToHospital(saved);

        return BookingResponse.from(saved);
    }

    public PageResponse<BookingResponse> getMyBookings(Pageable pageable) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Page<Booking> page = bookingRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        return PageResponse.from(page, BookingResponse::from);
    }

    public BookingResponse getMyBookingById(Long id) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (!booking.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancelMyBooking(Long id) {
        Long patientId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (!booking.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("This booking does not belong to you");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot be cancelled");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("This booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        log.info("Booking #{} cancelled by patient {}", updated.getId(), patientId);
        notificationService.notifyBookingStatusUpdateToPatient(updated);

        return BookingResponse.from(updated);
    }

    // ==== Admin Operations

    public PageResponse<AdminBookingResponse> getAllForAdmin(Pageable pageable, BookingStatus status) {
        Page<Booking> page = (status == null)
                ? bookingRepository.findAllByOrderByCreatedAtDesc(pageable)
                : bookingRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return PageResponse.from(page, AdminBookingResponse::from);
    }

    public AdminBookingResponse getByIdForAdmin(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return AdminBookingResponse.from(booking);
    }

    @Transactional
    public AdminBookingResponse updateStatus(Long id, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            throw new BadRequestException("Booking is already " + newStatus);
        }
        if (oldStatus == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot have their status changed");
        }

        booking.setStatus(newStatus);
        if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
            booking.setAdminNotes(request.getAdminNotes());
        }

        Booking updated = bookingRepository.save(booking);
        log.info("Booking #{} status changed: {} -> {}", id, oldStatus, newStatus);

        // Notify patient of the status change
        notificationService.notifyBookingStatusUpdateToPatient(updated);

        return AdminBookingResponse.from(updated);
    }

    @Transactional
    public AdminBookingResponse updateAdminNotes(Long id, String adminNotes) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        booking.setAdminNotes(adminNotes);
        return AdminBookingResponse.from(bookingRepository.save(booking));
    }

    // ==== Validation ===

    private void validateBookingRequest(BookingRequest request) {
        if (request.getBookingType() == BookingType.CONSULTATION) {
            if (request.getDepartmentId() == null) {
                throw new BadRequestException("Department is required for a consultation booking");
            }
            if (request.getPackageId() != null || request.getPackageTierId() != null) {
                throw new BadRequestException("Package fields should not be provided for a consultation booking");
            }
        } else if (request.getBookingType() == BookingType.PACKAGE) {
            if (request.getPackageId() == null) {
                throw new BadRequestException("Package is required for a package booking");
            }
            if (request.getDepartmentId() != null) {
                throw new BadRequestException("Department field should not be provided for a package booking");
            }
        }
    }
}
