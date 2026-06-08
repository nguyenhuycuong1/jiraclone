package com.jiraclone.service;

import com.jiraclone.dto.organization.OrganizationRequest;
import com.jiraclone.entity.Organization;
import com.jiraclone.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Organization createOrganization(OrganizationRequest request) {
        Organization organization = new Organization();
        organization.setOrgName(request.getOrgName());
        organization.setSlug(request.getSlug());
        organization.setStatus(request.getStatus());
        return organizationRepository.save(organization);
    }

    public Organization getOrganizationById(UUID id) {
        return organizationRepository.findById(id).orElse(null);
    }

    @Transactional
    public Organization updateOrganization(OrganizationRequest request, UUID id) {
        Organization organization = organizationRepository.findById(id).orElse(null);
        if (organization != null) {
            organization.setOrgName(request.getOrgName());
            organization.setSlug(request.getSlug());
            organization.setStatus(request.getStatus());
            return organizationRepository.save(organization);
        }
        return null;
    }

    @Transactional
    public void deleteOrganization(UUID id) {
        organizationRepository.deleteById(id);
    }
}
