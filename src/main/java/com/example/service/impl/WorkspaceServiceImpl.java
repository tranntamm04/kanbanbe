package com.example.service.impl;

import com.example.dto.workspace.WorkspaceRequest;
import com.example.dto.workspace.WorkspaceResponse;
import com.example.entity.*;
import com.example.entity.Workspace;
import com.example.entity.WorkspaceMember;
import com.example.entity.WorkspaceRole;
import com.example.exception.AppException;
import com.example.repository.*;
import com.example.service.WorkspaceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceInviteRepository inviteRepository;
    private final ProjectRepository projectRepository;
    private final BoardColumnRepository columnRepository;
    private final CommentRepository commentRepository;
    private final ActivityRepository activityRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public WorkspaceResponse create(WorkspaceRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found"));

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        workspaceRepository.save(workspace);

        // Add OWNER
        WorkspaceMember member = WorkspaceMember.builder()
                .user(user)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .build();

        memberRepository.save(member);

        return map(workspace);
    }

    @Override
    public List<WorkspaceResponse> getMyWorkspaces(Long userId) {
        return memberRepository.findByUserId(userId)
                .stream()
                .map(m -> map(m.getWorkspace()))
                .toList();
    }

    @Override
    public WorkspaceResponse getById(Long id, Long userId) {

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new AppException("Workspace not found"));

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(userId, id);

        if (!isMember) {
            throw new AppException("Access denied");
        }

        return map(workspace);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {

        WorkspaceMember member = memberRepository
                .findByUserIdAndWorkspaceId(userId, id)
                .orElseThrow(() -> new AppException("Not member"));

        if (member.getRole() != WorkspaceRole.OWNER) {
            throw new AppException("Only owner can delete workspace");
        }

        List<Task> tasks = taskRepository.findByProjectWorkspaceId(id);
        List<Long> taskIds = tasks.stream().map(Task::getId).toList();

        if (!taskIds.isEmpty()) {
            commentRepository.deleteByTaskIdIn(taskIds);
            activityRepository.deleteByTaskIdIn(taskIds);
            notificationRepository.deleteByTaskIdIn(taskIds);
            taskRepository.deleteByWorkspaceId(id);
        }

        columnRepository.deleteByProjectWorkspaceId(id);
        activityRepository.deleteByProjectWorkspaceId(id);
        notificationRepository.deleteByWorkspaceId(id);
        projectRepository.deleteByWorkspaceId(id);
        inviteRepository.deleteByWorkspaceId(id);
        memberRepository.deleteByWorkspaceId(id);
        workspaceRepository.deleteById(id);
    }

    private WorkspaceResponse map(Workspace w) {
        return WorkspaceResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .createdBy(w.getCreatedBy().getUsername())
                .createdAt(w.getCreatedAt())
                .build();
    }
}