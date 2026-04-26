package com.example.service;

import com.example.config.CustomUserDetails;
import com.example.entity.User;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User get() {

        var auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Unauthorized");
        }

        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }
}