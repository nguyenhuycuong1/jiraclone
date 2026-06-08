package com.jiraclone.service;

import com.jiraclone.config.TenantContextHolder;
import com.jiraclone.dto.project.ProjectRequest;
import com.jiraclone.entity.Organization;
import com.jiraclone.entity.Project;
import com.jiraclone.repository.OrganizationRepository;
import com.jiraclone.repository.ProjectRepository;
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

}
