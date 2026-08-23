package com.jiraclone.controller;

import com.jiraclone.dto.user.AccountUserResponse;
import com.jiraclone.repository.UserRepository;
import com.jiraclone.security.CustomUserDetails;
import com.jiraclone.service.AvatarService;
import com.jiraclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AvatarService avatarService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<AccountUserResponse>> getAll() {
        List<AccountUserResponse> users = userRepository.findAll()
                .stream()
                .map(AccountUserResponse::new)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/account")
    public ResponseEntity<AccountUserResponse> getAccount() {
        return ResponseEntity.ok(userService.getAccount());
    }

    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        UUID userId = userDetails.getId();
        String avatarUrl = avatarService.uploadAvatar(userId, file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }
}
