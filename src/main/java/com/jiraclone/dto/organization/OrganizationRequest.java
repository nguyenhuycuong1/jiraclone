package com.jiraclone.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrganizationRequest {
    private String orgName;
    private String slug;
    private String status;
}
