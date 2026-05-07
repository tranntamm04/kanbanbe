package com.example.service.impl;

import com.example.dto.workspace.WorkspaceRequest;
import com.example.dto.workspace.WorkspaceResponse;
import com.example.dto.workspace.InviteRequest;
import com.example.dto.workspace.WorkspaceMemberResponse;
import com.example.entity.*;
import com.example.exception.AppException;
import com.example.repository.*;
import com.example.service.WorkspaceService;
import com.example.service.EmailService;
import com.example.service.NotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final EmailService emailService;
    private final NotificationService notificationService;

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

    @Override
    public void inviteUser(Long workspaceId, InviteRequest request, Long userId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new AppException("Workspace not found"));

        WorkspaceMember inviter = memberRepository
                .findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new AppException("Not a member of this workspace"));

        if (inviter.getRole() != WorkspaceRole.OWNER
                && inviter.getRole() != WorkspaceRole.ADMIN) {
            throw new AppException("Only owner or admin can invite users");
        }

        String email = request.getEmail().trim().toLowerCase();

        // Check user already member
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null
                && memberRepository.existsByUserIdAndWorkspaceId(
                        existingUser.getId(),
                        workspaceId)) {

            throw new AppException("User is already a member");
        }

        // Find old invite
        WorkspaceInvite oldInvite = inviteRepository
                .findByEmailAndWorkspaceId(email, workspaceId)
                .orElse(null);

        // Nếu invite cũ chưa accept -> xoá để gửi lại
        if (oldInvite != null && !oldInvite.isAccepted()) {
            inviteRepository.delete(oldInvite);
        }

        WorkspaceRole role =
                WorkspaceRole.valueOf(request.getRole().toUpperCase());

        String token = UUID.randomUUID().toString();

        WorkspaceInvite invite = WorkspaceInvite.builder()
                .email(email)
                .workspace(workspace)
                .invitedBy(inviter.getUser())
                .role(role)
                .token(token)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .accepted(false)
                .build();

        inviteRepository.save(invite);

        emailService.sendInviteEmail(
                email,
                workspace.getName(),
                inviter.getUser().getUsername(),
                token
        );

        // Tạo notification nếu user đã tồn tại trong hệ thống
        if (existingUser != null) {
            notificationService.notify(
                    existingUser,
                    "Bạn được mời tham gia workspace \"" + workspace.getName() + "\" với vai trò " + role.name().toLowerCase(),
                    null,
                    workspace
            );
        }
    }

    @Override
    @Transactional
    public void acceptInvite(String token, Long userId) {
        WorkspaceInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Invalid invite token"));

        if (invite.isAccepted()) {
            throw new AppException("Invite already accepted");
        }

        if (invite.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException("Invite expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found"));

        if (!user.getEmail().equals(invite.getEmail())) {
            throw new AppException("Email mismatch");
        }

        // Check if already a member
        if (memberRepository.existsByUserIdAndWorkspaceId(userId, invite.getWorkspace().getId())) {
            throw new AppException("Already a member");
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .user(user)
                .workspace(invite.getWorkspace())
                .role(invite.getRole())
                .build();

        memberRepository.save(member);

        invite.setAccepted(true);
        inviteRepository.save(invite);

        // Thông báo cho người mời
        notificationService.notify(
                invite.getInvitedBy(),
                user.getUsername() + " đã chấp nhận lời mời tham gia workspace \"" + invite.getWorkspace().getName() + "\"",
                null,
                invite.getWorkspace()
        );
    }

    @Override
    public List<WorkspaceMemberResponse> getMembers(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new AppException("Workspace not found"));

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);

        if (!isMember) {
            throw new AppException("Access denied");
        }

        return memberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(member -> WorkspaceMemberResponse.builder()
                        .id(member.getUser().getId())
                        .username(member.getUser().getUsername())
                        .email(member.getUser().getEmail())
                        .role(member.getRole().name())
                        .build())
                .toList();
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