package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.TeamMemberResponse;
import com.djio.grover_hospital.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamMemberService teamMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers() {
        return ResponseEntity.ok(ApiResponse.success(teamMemberService.getAllActive()));
    }
}