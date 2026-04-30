package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.PackageTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageTierRepository extends JpaRepository<PackageTier, Long> {

    List<PackageTier> findByHealthPackageIdAndIsActiveTrueOrderByDisplayOrderAsc(Long packageId);
}
