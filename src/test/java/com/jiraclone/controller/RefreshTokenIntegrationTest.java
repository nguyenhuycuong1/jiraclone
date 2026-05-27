package com.jiraclone.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiraclone.dto.auth.LoginRequest;
import com.jiraclone.dto.auth.RegisterRequest;
import com.jiraclone.repository.RefreshTokenRepository;
import com.jiraclone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RefreshTokenIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void login_returnsAccessTokenAndSetsRefreshCookie() throws Exception {
        register("alice", "alice@test.com", "secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("alice", "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    void refresh_returnsNewAccessTokenAndRotatesCookie() throws Exception {
        register("alice", "alice@test.com", "secret123");
        String firstToken = loginAndGetRefreshToken("alice", "secret123");

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie(firstToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

        String secondToken = parseRefreshTokenFromHeader(result);
        assertThat(secondToken).isNotEqualTo(firstToken);
    }

    @Test
    void refresh_withRevokedToken_returns401() throws Exception {
        register("alice", "alice@test.com", "secret123");
        String firstToken = loginAndGetRefreshToken("alice", "secret123");

        // First refresh — rotates token, firstToken is now revoked
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie(firstToken)))
                .andExpect(status().isOk());

        // Second call with the revoked token
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie(firstToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withNoCookie_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_revokesToken_andSubsequentRefreshFails() throws Exception {
        register("alice", "alice@test.com", "secret123");
        String token = loginAndGetRefreshToken("alice", "secret123");

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_revokeAll_invalidatesAllUserTokens() throws Exception {
        register("alice", "alice@test.com", "secret123");

        // Login twice — two separate refresh tokens
        String token1 = loginAndGetRefreshToken("alice", "secret123");
        String token2 = loginAndGetRefreshToken("alice", "secret123");

        // Logout with revokeAll=true
        mockMvc.perform(post("/api/auth/logout?revokeAll=true")
                        .cookie(refreshCookie(token1)))
                .andExpect(status().isNoContent());

        // Both tokens should be revoked
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie(token1)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie(token2)))
                .andExpect(status().isUnauthorized());
    }

    // --- helpers ---

    private void register(String username, String email, String password) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    private String loginAndGetRefreshToken(String usernameOrEmail, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(usernameOrEmail, password)))
                .andExpect(status().isOk())
                .andReturn();
        return parseRefreshTokenFromHeader(result);
    }

    private String loginBody(String usernameOrEmail, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail(usernameOrEmail);
        req.setPassword(password);
        return objectMapper.writeValueAsString(req);
    }

    // Controller dùng response.addHeader(SET_COOKIE, ...) thay vì response.addCookie(),
    // nên getCookie() của MockMvc trả về null — parse thẳng từ header.
    private String parseRefreshTokenFromHeader(MvcResult result) {
        String header = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(header).isNotNull().contains("refreshToken=");
        return Arrays.stream(header.split(";"))
                .map(String::trim)
                .filter(p -> p.startsWith("refreshToken="))
                .map(p -> p.substring("refreshToken=".length()))
                .findFirst()
                .orElseThrow();
    }

    private MockCookie refreshCookie(String value) {
        MockCookie cookie = new MockCookie("refreshToken", value);
        cookie.setPath("/api/auth/refresh");
        return cookie;
    }
}
