package com.jiraclone.dto.org_member;

import com.jiraclone.enums.OrgRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgMemberRequest {
    private UUID userId;
    private UUID orgId;
    private OrgRole orgRole;
}
