package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByIsActiveTrueOrderByFullNameAsc();

    List<Doctor> findByDepartmentIdAndIsActiveTrueOrderByFullNameAsc(Long departmentId);
}
