package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.BulkDepartmentScheduleRequest;
import com.djio.grover_hospital.model.dto.request.DepartmentScheduleRequest;
import com.djio.grover_hospital.model.dto.response.DepartmentScheduleResponse;
import com.djio.grover_hospital.model.entity.Department;
import com.djio.grover_hospital.model.entity.DepartmentSchedule;
import com.djio.grover_hospital.repository.DepartmentRepository;
import com.djio.grover_hospital.repository.DepartmentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentScheduleService {

    private final DepartmentScheduleRepository scheduleRepository;
    private final DepartmentRepository departmentRepository;

    /* ===========================
       Read
       =========================== */

    @Transactional(readOnly = true)
    public List<DepartmentScheduleResponse> getByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return DepartmentScheduleResponse.fromList(
                scheduleRepository.findByDepartmentIdOrderByDayOfWeekAscStartTimeAsc(departmentId)
        );
    }

    /* ===========================
       Bulk replace (admin)
       =========================== */

    @Transactional
    public List<DepartmentScheduleResponse> replaceForDepartment(Long departmentId, BulkDepartmentScheduleRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        List<DepartmentScheduleRequest> incoming = request.getSchedules() == null ? List.of() : request.getSchedules();

        // Per-row validation: end must be after start
        for (DepartmentScheduleRequest r : incoming) {
            if (!r.getEndTime().isAfter(r.getStartTime())) {
                throw new BadRequestException(
                        "End time must be after start time for " + humanize(r.getDayOfWeek()) +
                                " (" + r.getStartTime() + " - " + r.getEndTime() + ")"
                );
            }
        }

        // No overlapping ranges within the same day. Touching (e.g. 12:00-14:00 then 14:00-17:00) is allowed.
        Map<DayOfWeek, List<DepartmentScheduleRequest>> byDay = incoming.stream()
                .collect(Collectors.groupingBy(DepartmentScheduleRequest::getDayOfWeek));

        for (Map.Entry<DayOfWeek, List<DepartmentScheduleRequest>> entry : byDay.entrySet()) {
            List<DepartmentScheduleRequest> sorted = entry.getValue().stream()
                    .sorted(Comparator.comparing(DepartmentScheduleRequest::getStartTime))
                    .toList();
            for (int i = 1; i < sorted.size(); i++) {
                LocalTime prevEnd = sorted.get(i - 1).getEndTime();
                LocalTime currStart = sorted.get(i).getStartTime();
                if (currStart.isBefore(prevEnd)) {
                    throw new BadRequestException(
                            "Overlapping time ranges on " + humanize(entry.getKey()) + ": " +
                                    sorted.get(i - 1).getStartTime() + "-" + prevEnd +
                                    " and " + currStart + "-" + sorted.get(i).getEndTime()
                    );
                }
            }
        }

        // Replace strategy: delete all then insert. Existing bookings are NOT touched (per chosen rule).
        scheduleRepository.deleteByDepartmentId(departmentId);
        scheduleRepository.flush();

        List<DepartmentSchedule> toSave = incoming.stream()
                .map(r -> DepartmentSchedule.builder()
                        .department(department)
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build())
                .collect(Collectors.toList());

        List<DepartmentSchedule> saved = scheduleRepository.saveAll(toSave);
        log.info("Replaced schedule for department {} with {} entries", departmentId, saved.size());

        return DepartmentScheduleResponse.fromList(saved);
    }

    /* ===========================
       Booking-time validation
       =========================== */

    /**
     * Validates that a booking's date/time falls inside the department's published schedule.
     * <p>
     * Rule: time must be >= a window's start and < that window's end.
     * (Start inclusive, end exclusive — so e.g. an 11:00–17:00 window accepts bookings
     * from 11:00 up to 16:59 but not 17:00, since the consultation would extend past closing.)
     * <p>
     * Called by {@code BookingService} on both create and reschedule.
     *
     * @throws BadRequestException if the department isn't open that day, or the time isn't inside any window
     */
    @Transactional(readOnly = true)
    public void validateBookingAgainstSchedule(Long departmentId, DayOfWeek dayOfWeek, LocalTime time) {
        List<DepartmentSchedule> slots = scheduleRepository.findByDepartmentIdAndDayOfWeek(departmentId, dayOfWeek);

        if (slots.isEmpty()) {
            throw new BadRequestException(
                    "This department is not available on " + humanize(dayOfWeek) + "."
            );
        }

        boolean inside = slots.stream().anyMatch(s ->
                !time.isBefore(s.getStartTime()) && time.isBefore(s.getEndTime())
        );

        if (!inside) {
            String windows = slots.stream()
                    .sorted(Comparator.comparing(DepartmentSchedule::getStartTime))
                    .map(s -> s.getStartTime() + "\u2013" + s.getEndTime())
                    .collect(Collectors.joining(", "));
            throw new BadRequestException(
                    "This department is not available at " + time + " on " + humanize(dayOfWeek) +
                            ". Available windows: " + windows + "."
            );
        }
    }

    /**
     * Validates the department is open at all on the given day-of-week.
     * Use at booking-create time, when only the date is known (no time yet).
     *
     * @throws BadRequestException if the department has no schedule for that day
     */
    @Transactional(readOnly = true)
    public void validateBookingDayAvailable(Long departmentId, DayOfWeek dayOfWeek) {
        List<DepartmentSchedule> slots = scheduleRepository.findByDepartmentIdAndDayOfWeek(departmentId, dayOfWeek);
        if (slots.isEmpty()) {
            throw new BadRequestException(
                    "This department is not available on " + humanize(dayOfWeek) + "."
            );
        }
    }

    private String humanize(DayOfWeek d) {
        String name = d.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}