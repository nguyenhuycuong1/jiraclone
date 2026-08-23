package com.jiraclone.dto.org_member;

import com.jiraclone.entity.Organization;
import com.jiraclone.enums.OrgRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgMemberResponse {
    private UUID id;
    private String orgName;
    private String slug;
    private OrgRole role;

    public OrgMemberResponse(Organization organization, OrgRole orgRole) {
        this.id = organization.getId();
        this.orgName = organization.getOrgName();
        this.slug = organization.getSlug();
        this.role = orgRole;
    }
}
