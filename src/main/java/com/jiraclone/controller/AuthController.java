package com.jiraclone.controller;

import com.jiraclone.dto.auth.*;
import com.jiraclone.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResult result = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new JwtResponse(
                result.getAccessToken(),
                result.getUsername(),
                result.getEmail()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LoginResult result = authService.refresh(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new JwtResponse(
                result.getAccessToken(),
                result.getUsername(),
                result.getEmail()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestParam(defaultValue = "false") boolean revokeAll,
            HttpServletResponse response)
    {
        if (refreshToken != null) {
            authService.logout(refreshToken, revokeAll);
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/api/auth")
                    .sameSite("Strict")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Boolean> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean isValid = authService.verifyOTP(request);
        return ResponseEntity.ok(isValid);
    }
}
