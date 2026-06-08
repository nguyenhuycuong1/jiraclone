package com.jiraclone.controller;

import com.jiraclone.dto.project.ProjectRequest;
import com.jiraclone.dto.project.ProjectResponse;
import com.jiraclone.entity.Project;
import com.jiraclone.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjectsByOrg() {
        List<ProjectResponse> projects = projectService.getProjectsByOrg().stream().map(ProjectResponse::new).toList();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        Project project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ProjectResponse(project));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest request) {
        Project project = projectService.createProject(request);
        return ResponseEntity.ok(new ProjectResponse(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@RequestBody ProjectRequest request, @PathVariable UUID id) {
        Project project = projectService.updateProject(request, id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ProjectResponse(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

}
