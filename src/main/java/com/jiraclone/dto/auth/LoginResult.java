package com.jiraclone.dto.auth;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String email;
}
