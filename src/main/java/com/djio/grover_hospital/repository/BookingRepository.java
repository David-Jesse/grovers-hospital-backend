package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.Booking;
import com.djio.grover_hospital.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Booking> findByPreferredDateAndStatusAndReminderSentForDateIsNull(
            LocalDate preferredDate, BookingStatus status
    );

    Page<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);
}