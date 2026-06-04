package com.jiraclone.security;

import lombok.Data;

import java.util.UUID;

@Data
public class JwtPayload {

    private String username;
    private String orgId;
}
