package com.jiraclone.dto.user;

import com.jiraclone.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UserResponse {

    private final UUID id;
    private final UUID orgId;
    private final String username;
    private final String email;
    private final String displayName;
    private final String avatarUrl;
    private final String role;
    private final LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt();
    }
}
