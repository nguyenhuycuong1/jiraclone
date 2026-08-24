package com.jiraclone.service;

import com.jiraclone.dto.auth.JwtResponse;
import com.jiraclone.dto.organization.OrganizationRequest;
import com.jiraclone.entity.Organization;

import java.util.UUID;

public interface OrganizationService {
    Organization createOrganization(OrganizationRequest request);

    Organization createOrganizationByUser(OrganizationRequest request);

    Organization getOrganizationById(UUID id);

    Organization updateOrganization(OrganizationRequest request, UUID id);

    void deleteOrganization(UUID id);

    JwtResponse setCurrentOrganization(UUID org_id);

    Boolean checkExistOrgName(String orgName);
}
