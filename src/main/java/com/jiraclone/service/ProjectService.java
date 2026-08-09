package com.jiraclone.service;

import com.jiraclone.config.TenantContextHolder;
import com.jiraclone.dto.project.ProjectRequest;
import com.jiraclone.dto.project_member.ProjectMemberRequest;
import com.jiraclone.dto.project_member.ProjectMemberResponse;
import com.jiraclone.entity.Organization;
import com.jiraclone.entity.Project;
import com.jiraclone.entity.ProjectMember;
import com.jiraclone.entity.User;
import com.jiraclone.repository.OrganizationRepository;
import com.jiraclone.repository.ProjectMemberRepository;
import com.jiraclone.repository.ProjectRepository;
import com.jiraclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<Project> getProjectsByOrg() {
        return projectRepository.findAll();
    }

    public Project getProjectById(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
    }

    @Transactional
    public Project createProject(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setKey(request.getKey());
        project.setType(request.getType());

        String orgId = TenantContextHolder.getTenantId();
        Organization org = organizationRepository.getReferenceById(UUID.fromString(orgId));
        project.setOrganization(org);

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(ProjectRequest request, UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectMemberResponse addMember(ProjectMemberRequest request) {
        ProjectMemberResponse result = new ProjectMemberResponse();
        ProjectMember projectMember = new ProjectMember();

        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        Project project = getProjectById(request.getProjectId());

        projectMember.setUser(user);
        projectMember.setProject(project);
        projectMember.setProjectRole(request.getProjectRole());

        projectMemberRepository.save(projectMember);

        result.setProjectId(projectMember.getId());
        result.setUserId(user.getId());
        result.setUserName(user.getDisplayName());
        result.setProjectId(project.getId());
        result.setProjectRole(projectMember.getProjectRole());

        return result;
    }

    @Transactional
    public List<ProjectMemberResponse> getMember(UUID projectId) {
        List<ProjectMember> projectMembers = projectMemberRepository.findAllMemberByProjectId(projectId);

        return projectMembers.stream().map(projectMember -> new ProjectMemberResponse(
                projectMember.getUser().getId(),
                projectMember.getProject().getId(),
                projectMember.getUser().getDisplayName(),
                projectMember.getProjectRole()
        )).toList();
    }

}
