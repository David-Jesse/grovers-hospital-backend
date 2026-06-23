package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.DepartmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface DepartmentScheduleRepository extends JpaRepository<DepartmentSchedule, Long> {

    List<DepartmentSchedule> findByDepartmentIdOrderByDayOfWeekAscStartTimeAsc(Long departmentId);

    List<DepartmentSchedule> findByDepartmentIdAndDayOfWeek(Long departmentId, DayOfWeek dayOfWeek);

    void deleteByDepartmentId(Long departmentId);
}