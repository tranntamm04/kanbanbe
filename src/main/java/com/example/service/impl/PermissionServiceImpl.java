package com.example.service.impl;

import com.example.entity.WorkspaceMember;
import com.example.entity.WorkspaceRole;
import com.example.exception.AppException;
import com.example.repository.WorkspaceMemberRepository;
import com.example.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final WorkspaceMemberRepository repo;

    @Override
    public WorkspaceMember get(Long wsId, Long userId) {
        return repo.findByUserIdAndWorkspaceId(userId, wsId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
    }

    @Override
    public void checkAdmin(Long wsId, Long userId) {
        WorkspaceMember m = get(wsId, userId);

        if (m.getRole() == WorkspaceRole.MEMBER) {
            throw new RuntimeException("Permission denied");
        }
    }
}