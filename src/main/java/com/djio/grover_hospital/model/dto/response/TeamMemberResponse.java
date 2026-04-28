package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {

    private Long id;
    private String fullName;
    private String title;
    private String bio;
    private String photoUrl;
    private Integer displayOrder;

    public static TeamMemberResponse from (TeamMember teamMember) {
        return TeamMemberResponse.builder()
                .id(teamMember.getId())
                .fullName(teamMember.getFullName())
                .title(teamMember.getTitle())
                .bio(teamMember.getBio())
                .photoUrl(teamMember.getPhotoUrl())
                .displayOrder(teamMember.getDisplayOrder())
                .build();
    }
}
