package com.djio.grover_hospital.controller;


import com.djio.grover_hospital.model.dto.request.TeamMemberRequest;
import com.djio.grover_hospital.model.dto.response.ApiResponse;
import com.djio.grover_hospital.model.dto.response.TeamMemberResponse;
import com.djio.grover_hospital.service.TeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/team")
@RequiredArgsConstructor
public class AdminTeamController {

    private final TeamMemberService teamMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(teamMemberService.getAllForAdmin()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamMemberResponse>> create(
            @Valid @RequestBody TeamMemberRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team member added", teamMemberService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TeamMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Team member updated", teamMemberService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> delete(@PathVariable Long id) {
        teamMemberService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team member deleted", null));
    }
}
