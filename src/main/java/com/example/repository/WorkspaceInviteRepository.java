package com.example.repository;

import com.example.entity.WorkspaceInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, Long> {
    void deleteByWorkspaceId(Long workspaceId);
    Optional<WorkspaceInvite> findByToken(String token);
    boolean existsByEmailAndWorkspaceId(String email, Long workspaceId);
    Optional<WorkspaceInvite> findByEmailAndWorkspaceId(String email, Long workspaceId);
}