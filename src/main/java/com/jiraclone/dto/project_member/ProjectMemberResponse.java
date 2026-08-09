package com.jiraclone.dto.project_member;

import com.jiraclone.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberResponse {
    private UUID userId;
    private UUID projectId;
    private String userName;
    private ProjectRole projectRole;
}
