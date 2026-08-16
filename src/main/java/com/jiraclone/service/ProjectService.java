package com.jiraclone.service;

import com.jiraclone.dto.project.ProjectRequest;
import com.jiraclone.dto.project_member.ProjectMemberRequest;
import com.jiraclone.dto.project_member.ProjectMemberResponse;
import com.jiraclone.entity.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    List<Project> getProjectsByOrg();

    Project getProjectById(UUID id);

    Project createProject(ProjectRequest request);

    Project updateProject(ProjectRequest request, UUID id);

    void deleteProject(UUID id);

    ProjectMemberResponse addMember(ProjectMemberRequest request);

    List<ProjectMemberResponse> getMember(UUID projectId);
}
