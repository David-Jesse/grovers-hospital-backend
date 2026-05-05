package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.ResultFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultFileRepository extends JpaRepository<ResultFile, Long> {
}
