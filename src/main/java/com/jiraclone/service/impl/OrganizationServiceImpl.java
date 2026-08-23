package com.jiraclone.service.impl;

import com.jiraclone.config.TenantContextHolder;
import com.jiraclone.dto.auth.JwtResponse;
import com.jiraclone.dto.organization.OrganizationRequest;
import com.jiraclone.entity.OrgMember;
import com.jiraclone.entity.Organization;
import com.jiraclone.entity.User;
import com.jiraclone.repository.OrganizationRepository;
import com.jiraclone.repository.UserRepository;
import com.jiraclone.security.CustomUserDetails;
import com.jiraclone.security.JwtTokenProvider;
import com.jiraclone.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Transactional
    public JwtResponse setCurrentOrganization(UUID org_id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found!"));

        List<OrgMember> orgMemberList = user.getOrgMembers();

        OrgMember orgMemberMatch = orgMemberList.stream()
                .filter(orgMember -> (orgMember.getOrganization().getId().equals(org_id)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Organization not found!"));

        String newToken = jwtTokenProvider.generateToken(userDetails, orgMemberMatch.getOrganization().getId().toString());

        return new JwtResponse(
                newToken,
                userDetails.getUsername(),
                userDetails.getEmail()
        );
    }
}
