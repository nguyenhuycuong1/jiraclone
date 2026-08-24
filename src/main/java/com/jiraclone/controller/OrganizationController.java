package com.jiraclone.controller;

import com.jiraclone.dto.organization.OrganizationRequest;
import com.jiraclone.dto.organization.OrganizationResponse;
import com.jiraclone.entity.Organization;
import com.jiraclone.service.OrganizationService;
import jakarta.validation.Valid;
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
                organization.getDescription(),
                organization.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationRequest organizationRequest) {
       Organization organization = organizationService.createOrganization(organizationRequest);
        return ResponseEntity.ok(new OrganizationResponse(
                organization.getId(),
                organization.getOrgName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getDescription(),
                organization.getCreatedAt()
        ));
    }

    @PostMapping("/create-by-owner")
    public ResponseEntity<OrganizationResponse> createOrganizationByUser(@Valid @RequestBody OrganizationRequest organizationRequest) {
       Organization organization = organizationService.createOrganizationByUser(organizationRequest);
        return ResponseEntity.ok(new OrganizationResponse(
                organization.getId(),
                organization.getOrgName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getDescription(),
                organization.getCreatedAt()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(@Valid @RequestBody OrganizationRequest organizationRequest, @PathVariable UUID id) {
        Organization organization = organizationService.updateOrganization(organizationRequest, id);
        return ResponseEntity.ok(new OrganizationResponse(
                organization.getId(),
                organization.getOrgName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getDescription(),
                organization.getCreatedAt()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-exist-org-name")
    public ResponseEntity<Boolean> checkExistOrgName(@RequestBody String orgName) {
        Boolean result = organizationService.checkExistOrgName(orgName);
        return ResponseEntity.ok(result);
    }
}
