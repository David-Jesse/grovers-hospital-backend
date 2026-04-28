package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.ConsultantSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultantScheduleRepository extends JpaRepository<ConsultantSchedule, Long> {

    List<ConsultantSchedule> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<ConsultantSchedule> findByDepartmentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long departmentId);
}
