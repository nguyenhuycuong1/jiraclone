package com.jiraclone.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrganizationResponse {
    private UUID id;
    private String orgName;
    private String slug;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}
