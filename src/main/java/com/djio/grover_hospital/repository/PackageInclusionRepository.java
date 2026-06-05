package com.djio.grover_hospital.repository;

import com.djio.grover_hospital.model.entity.PackageInclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageInclusionRepository extends JpaRepository<PackageInclusion, Long> {

    List<PackageInclusion> findByHealthPackageIdOrderByDisplayOrderAsc(Long packageId);
}