package com.example.service;

import com.example.entity.WorkspaceMember;

public interface PermissionService {

    WorkspaceMember get(Long wsId, Long userId);
    void checkAdmin(Long wsId, Long userId);
}