package com.jiraclone.service;

import com.jiraclone.dto.user.UserResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    UserResponse getAccount();
}
