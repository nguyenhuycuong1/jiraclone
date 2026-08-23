package com.jiraclone.controller;

import com.jiraclone.dto.organization.OrganizationRequest;
import com.jiraclone.dto.organization.OrganizationResponse;
import com.jiraclone.entity.Organization;
import com.jiraclone.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable UUID id) {
        Organization organization = organizationService.getOrganizationById(id);
        OrganizationResponse response = new OrganizationResponse(
                organization.getId(),
                organization.getOrgName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    public ResponseEntity<OrganizationResponse> createOrganization(@RequestBody OrganizationRequest organizationRequest) {
       Organization organization = organizationService.createOrganization(organizationRequest);
        return ResponseEntity.ok(new OrganizationResponse(
                organization.getId(),
                organization.getOrgName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getCreatedAt()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(@RequestBody OrganizationRequest organizationRequest, @PathVariable UUID id) {
        Organization organization = organizationService.updateOrganization(organizationRequest, id);
        return ResponseEntity.ok(new OrganizationResponse(
                organization.getId(),
                organization.getOrgName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getCreatedAt()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}
