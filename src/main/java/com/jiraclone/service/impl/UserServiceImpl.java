package com.jiraclone.service.impl;

import com.jiraclone.dto.user.AccountUserResponse;
import com.jiraclone.entity.User;
import com.jiraclone.repository.UserRepository;
import com.jiraclone.security.CustomUserDetails;
import com.jiraclone.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public AccountUserResponse getAccount() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        return new AccountUserResponse(user);
    }
}
