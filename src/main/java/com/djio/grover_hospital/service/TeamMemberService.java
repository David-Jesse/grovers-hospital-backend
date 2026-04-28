package com.djio.grover_hospital.service;


import com.djio.grover_hospital.model.dto.response.TeamMemberResponse;
import com.djio.grover_hospital.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    public List<TeamMemberResponse> getAllActive() {
        return teamMemberRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
    }
}