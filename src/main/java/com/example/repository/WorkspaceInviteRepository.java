package com.example.repository;

import com.example.entity.WorkspaceInvite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {
    void deleteByWorkspaceId(Long workspaceId);
}