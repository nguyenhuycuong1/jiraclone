package com.jiraclone.service;

import com.jiraclone.dto.auth.LoginRequest;
import com.jiraclone.dto.auth.LoginResult;
import com.jiraclone.dto.auth.RegisterRequest;
import com.jiraclone.dto.auth.VerifyOtpRequest;
import com.jiraclone.entity.RefreshToken;
import com.jiraclone.entity.User;
import com.jiraclone.enums.UserState;
import com.jiraclone.exception.AppException;
import com.jiraclone.repository.RefreshTokenRepository;
import com.jiraclone.repository.UserRepository;
import com.jiraclone.security.CustomUserDetails;
import com.jiraclone.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 30;
    private static final Map<Integer, Long> DELAY_MAP = Map.of(
            1, 1_000L, // 1 second
            2, 2_000L, // 2 seconds
            3, 4_000L, // 4 seconds
            4, 8_000L // 8 seconds
    );

    private String failLoginKey(String usernameOrEmail) {
        return "login:fail:" + usernameOrEmail;
    }
    private String lockLoginKey(String usernameOrEmail) {
        return "login:lock:" + usernameOrEmail;
    }
    private String cooldownLoginKey(String usernameOrEmail) {
        return "login:cooldown:" + usernameOrEmail;
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        String key = request.getUsernameOrEmail();

        Boolean locked = (Boolean) redisTemplate.opsForValue().get(lockLoginKey(key));
        if (Boolean.TRUE.equals(locked)) {
            throw new AppException(HttpStatus.TOO_MANY_REQUESTS,
                    "Account locked due to too many failed login attempts. Try again after " + LOCK_DURATION_MINUTES + " minutes.");
        }

        Boolean inCooldown = (Boolean) redisTemplate.opsForValue().get(cooldownLoginKey(key));
        if (Boolean.TRUE.equals(inCooldown)) {
            Long remainMs = redisTemplate.getExpire(cooldownLoginKey(key), TimeUnit.MILLISECONDS);
            long remainSec = (long) Math.ceil(remainMs / 1000.0);
            throw new AppException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Please wait " + remainSec + " second(s) before trying again.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(key, request.getPassword()));
        } catch (BadCredentialsException e) {
            Long failCount = redisTemplate.opsForValue().increment(failLoginKey(key));
            if (failCount != null && failCount == 1) {
                redisTemplate.expire(failLoginKey(key), LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            }
            if (failCount != null && failCount >= MAX_LOGIN_ATTEMPTS) {
                redisTemplate.opsForValue().set(lockLoginKey(key), true, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
                redisTemplate.delete(failLoginKey(key));
                throw new AppException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many failed login attempts. Account locked for " + LOCK_DURATION_MINUTES + " minutes.");
            }
            Long delayMs = DELAY_MAP.get(failCount != null ? failCount.intValue() : 1);
            if (delayMs != null) {
                redisTemplate.opsForValue().set(cooldownLoginKey(key), true, delayMs, TimeUnit.MILLISECONDS);
            }
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        redisTemplate.delete(failLoginKey(key));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getStatus() == UserState.PENDING_VERIFY) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Account pending verification. Please verify your email.");
        }
        if (user.getStatus() == UserState.SUSPENDED) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account suspended. Please contact support.");
        }
        if (user.getStatus() == UserState.DELETED) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account deleted. Please contact support.");
        }
        String token = jwtTokenProvider.generateToken(toUserDetails(user));
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
        sendOTP(request.getEmail());
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
        String newToken = jwtTokenProvider.generateToken(toUserDetails(user));

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

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextInt(900000) + 100000); // 6-digit OTP
    }

    @Transactional
    public Boolean verifyOTP(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getStatus() != UserState.PENDING_VERIFY) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User already verified");
        }

        String storedOTP = (String) redisTemplate.opsForValue().get("otp:" + request.getEmail());
        if (storedOTP == null || !storedOTP.equals(request.getOtp())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        user.setStatus(UserState.ACTIVE);
        userRepository.save(user);

        redisTemplate.delete("otp:" + request.getEmail());

        return true;
    }

    public void resendOTP(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getStatus() != UserState.PENDING_VERIFY) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User already verified");
        }

        sendOTP(email);
    }

    public void sendOTP(String email) {
        String otp = generateOTP();
        redisTemplate.opsForValue().set("otp:" + email, otp, 10, TimeUnit.of(ChronoUnit.MINUTES));

        emailService.sendMail(email, "OTP for registering a Jiraclone account", "Your OTP code is: " + otp);
    }

    private CustomUserDetails toUserDetails(User user) {
        UUID orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                orgId,
                user.getAuthorities()
        );
    }
}
