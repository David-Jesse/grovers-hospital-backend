package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.TeamMemberRequest;
import com.djio.grover_hospital.model.dto.response.TeamMemberResponse;
import com.djio.grover_hospital.model.entity.TeamMember;
import com.djio.grover_hospital.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    // === Public Read ===
    public List<TeamMemberResponse> getAllActive() {
        return teamMemberRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    // === Admin ===

    public List<TeamMemberResponse> getAllForAdmin() {
        return teamMemberRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    @Transactional
    public TeamMemberResponse create(TeamMemberRequest request) {
        TeamMember member = TeamMember.builder()
                .fullName(request.getFullName())
                .title(request.getTitle())
                .bio(request.getBio())
                .photoUrl(request.getPhotoUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return TeamMemberResponse.from(teamMemberRepository.save(member));
    }

    @Transactional
    public TeamMemberResponse update(Long id, TeamMemberRequest request) {
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team member", "id", id));

        member.setFullName(request.getFullName());
        member.setTitle(request.getBio());
        member.setBio(request.getBio());
        member.setPhotoUrl(request.getPhotoUrl());
        if (request.getDisplayOrder() != null) member.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) member.setIsActive(request.getIsActive());

        return TeamMemberResponse.from(teamMemberRepository.save(member));
    }

    @Transactional
    public void delete(Long id) {
        if (!teamMemberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team member", "id", id);
        }

        teamMemberRepository.deleteById(id);
    }
}