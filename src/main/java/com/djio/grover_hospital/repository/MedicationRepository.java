package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByPatientIdOrderByIsActiveDescStartDateDesc(Long patientId);

    List<Medication> findByPatientIdAndIsActiveOrderByStartDateDesc(Long patientId, Boolean isActive);
}