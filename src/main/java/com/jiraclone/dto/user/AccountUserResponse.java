package com.jiraclone.dto.user;

import com.jiraclone.dto.org_member.OrgMemberResponse;
import com.jiraclone.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountUserResponse {

    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String role;
    private List<OrgMemberResponse> organizations;
    private LocalDateTime createdAt;

    public AccountUserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
        this.role = user.getRole().name();
        this.organizations = user.getOrgMembers().stream().map(
                orgMember -> new OrgMemberResponse(
                        orgMember.getOrganization(),
                        orgMember.getOrgRole()
                )
        ).toList();
        this.createdAt = user.getCreatedAt();
    }
}
