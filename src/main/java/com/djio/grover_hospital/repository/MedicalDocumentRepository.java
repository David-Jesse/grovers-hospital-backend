package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.MedicalDocument;
import com.djio.grover_hospital.model.enums.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {

    List<MedicalDocument> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<MedicalDocument> findByPatientIdAndCategoryOrderByCreatedAtDesc(Long patientId, DocumentCategory category);
}