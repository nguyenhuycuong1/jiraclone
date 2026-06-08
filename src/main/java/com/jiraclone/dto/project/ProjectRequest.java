package com.jiraclone.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ProjectRequest {
    private String name;
    private String description;
    private String type;
    private String key;
}
