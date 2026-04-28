package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findbyEmail(String email);

    boolean existsByEmail(String email);
}