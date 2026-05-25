package com.jiraclone.service;

import com.jiraclone.dto.auth.JwtResponse;
import com.jiraclone.dto.auth.LoginRequest;
import com.jiraclone.dto.auth.LoginResult;
import com.jiraclone.dto.auth.RegisterRequest;
import com.jiraclone.entity.RefreshToken;
import com.jiraclone.entity.User;
import com.jiraclone.exception.AppException;
import com.jiraclone.repository.RefreshTokenRepository;
import com.jiraclone.repository.UserRepository;
import com.jiraclone.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = generateRefreshToken(user).getToken();
        return new LoginResult(token, refreshToken, user.getUsername(), user.getEmail());
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Username already taken");
        }
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
                .build();
        userRepository.save(user);
    }

    private RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken);
        if (token == null || token.getRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        User user = token.getUser();
        // Create new access token
        String newToken = jwtTokenProvider.generateToken(user);

        // revoke old refresh token
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        // generate new refresh token
        String newRefreshToken = generateRefreshToken(user).getToken();

        return new LoginResult(newToken, newRefreshToken, user.getUsername(), user.getEmail());
    }

    @Transactional
    public void logout(String refreshToken, boolean revokeAll) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken);
        if (token != null) {
            if (revokeAll) {
                refreshTokenRepository.revokeAllByUserId(token.getUser().getId());
            } else {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            }
        }
    }
}
