package com.jiraclone.security;

import lombok.Data;

@Data
public class JwtPayload {

    private String username;
    private String orgId;
}
