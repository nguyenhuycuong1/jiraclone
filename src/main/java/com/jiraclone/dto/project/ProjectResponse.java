package com.jiraclone.dto.project;

import com.jiraclone.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ProjectResponse {
    private String name;
    private String description;
    private String type;
    private String key;

    public ProjectResponse(Project project) {
        this.name = project.getName();
        this.description = project.getDescription();
        this.type = project.getType();
        this.key = project.getKey();
    }
}
