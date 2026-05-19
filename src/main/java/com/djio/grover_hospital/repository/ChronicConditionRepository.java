package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.ChronicCondition;
import com.djio.grover_hospital.model.enums.ConditionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChronicConditionRepository extends JpaRepository<ChronicCondition, Long> {

    List<ChronicCondition> findByPatientIdOrderByDiagnosedDateDesc(Long patientId);

    List<ChronicCondition> findByPatientIdAndStatusOrderByDiagnosedDateDesc(Long patientId, ConditionStatus status);
}