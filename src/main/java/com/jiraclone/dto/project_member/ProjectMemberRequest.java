package com.jiraclone.dto.project_member;

import com.jiraclone.enums.ProjectRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectMemberRequest {
    @NotBlank(message = "Thông tin thành viên không được để trống")
    private UUID userId;
    @NotBlank(message = "Thông tin dự án không được để trống")
    private UUID projectId;
    @NotBlank(message = "Vai trò dự án không được để trống")
    private ProjectRole projectRole;
}
