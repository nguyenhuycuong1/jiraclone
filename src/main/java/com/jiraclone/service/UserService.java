package com.jiraclone.service;

import com.jiraclone.dto.user.AccountUserResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    AccountUserResponse getAccount();
}
