package com.jiraclone.dto.auth;

import lombok.Getter;

@Getter
public class JwtResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final String username;
    private final String email;

    public JwtResponse(String accessToken, String username, String email) {
        this.accessToken = accessToken;
        this.username = username;
        this.email = email;
    }
}
