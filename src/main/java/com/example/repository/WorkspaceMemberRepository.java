package com.example.repository;

import com.example.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    List<WorkspaceMember> findByUserId(Long userId);
    boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId);
    Optional<WorkspaceMember> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);
    void deleteByWorkspaceId(Long workspaceId);
    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);
}