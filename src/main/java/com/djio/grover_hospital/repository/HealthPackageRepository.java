package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.HealthPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthPackageRepository extends JpaRepository<HealthPackage, Long> {

    Optional<HealthPackage> findBySlug(String slug);

    List<HealthPackage> findByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsBySlug(String slug);
}
