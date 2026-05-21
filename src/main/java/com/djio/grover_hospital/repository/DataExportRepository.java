package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.DataExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DataExportRepository extends JpaRepository<DataExportJob, Long> {

    Optional<DataExportJob> findByToken(String token);
}