package com.djio.grover_hospital.repository;


import com.djio.grover_hospital.model.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByIsActiveTrueOrderByDisplayOrderAsc();
}
