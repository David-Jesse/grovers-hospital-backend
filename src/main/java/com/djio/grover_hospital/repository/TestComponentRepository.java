package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.TestComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestComponentRepository extends JpaRepository<TestComponent, Long> {

    List<TestComponent> findByResultIdOrderByDisplayOrderAscIdAsc(Long resultId);

    void deleteByResultId(Long resultId);

    long countByResultId(Long resultId);
}