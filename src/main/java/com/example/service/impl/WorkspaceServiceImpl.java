package com.example.service.impl;

import com.example.dto.workspace.InviteRequest;
import com.example.dto.workspace.InviteResponse;
import com.example.dto.workspace.WorkspaceMemberResponse;
import com.example.dto.workspace.WorkspaceRequest;
import com.example.dto.workspace.WorkspaceResponse;
import com.example.entity.Activity;
import com.example.entity.BoardColumn;
import com.example.entity.Task;
import com.example.entity.User;
import com.example.entity.Workspace;
import com.example.entity.WorkspaceInvite;
import com.example.entity.WorkspaceMember;
import com.example.entity.WorkspaceRole;
import com.example.exception.AppException;
import com.example.repository.ActivityRepository;
import com.example.repository.BoardColumnRepository;
import com.example.repository.CommentRepository;
import com.example.repository.NotificationRepository;
import com.example.repository.ProjectRepository;
import com.example.repository.TaskRepository;
import com.example.repository.UserRepository;
import com.example.repository.WorkspaceInviteRepository;
import com.example.repository.WorkspaceMemberRepository;
import com.example.repository.WorkspaceRepository;
import com.example.service.EmailService;
import com.example.service.NotificationService;
import com.example.service.WorkspaceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceServiceImpl.class);

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

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public WorkspaceResponse create(WorkspaceRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found"));

        Workspace workspace = Workspace.builder()
                .name(request.getName().trim())
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
    @Transactional
    public InviteResponse inviteUser(Long workspaceId, InviteRequest request, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new AppException("Workspace not found"));

        WorkspaceMember inviter = memberRepository
                .findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new AppException("Not a member of this workspace"));

        if (inviter.getRole() != WorkspaceRole.OWNER && inviter.getRole() != WorkspaceRole.ADMIN) {
            throw new AppException("Only owner or admin can invite users");
        }

        String email = request.getEmail().trim().toLowerCase();
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null && memberRepository.existsByUserIdAndWorkspaceId(existingUser.getId(), workspaceId)) {
            throw new AppException("User is already a member");
        }

        WorkspaceInvite oldInvite = inviteRepository.findByEmailAndWorkspaceId(email, workspaceId).orElse(null);
        if (oldInvite != null && !oldInvite.isAccepted()) {
            inviteRepository.delete(oldInvite);
        }

        WorkspaceRole role = WorkspaceRole.valueOf(request.getRole().trim().toUpperCase());
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

        String inviteUrl = buildInviteUrl(token);
        boolean emailSent = sendInviteEmail(email, workspace, inviter.getUser(), token);

        if (existingUser != null) {
            notificationService.notify(
                    existingUser,
                    "Bạn được mời tham gia workspace \"" + workspace.getName() + "\" với vai trò " + role.name().toLowerCase(),
                    null,
                    workspace
            );
        }

        return InviteResponse.builder()
                .email(email)
                .role(role.name())
                .inviteUrl(inviteUrl)
                .emailSent(emailSent)
                .build();
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

        if (!user.getEmail().equalsIgnoreCase(invite.getEmail())) {
            throw new AppException("Email mismatch");
        }

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

        notificationService.notify(
                invite.getInvitedBy(),
                user.getUsername() + " đã chấp nhận lời mời tham gia workspace \"" + invite.getWorkspace().getName() + "\"",
                null,
                invite.getWorkspace()
        );
    }

    @Override
    public List<WorkspaceMemberResponse> getMembers(Long workspaceId, Long userId) {
        workspaceRepository.findById(workspaceId)
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

    private boolean sendInviteEmail(String email, Workspace workspace, User inviter, String token) {
        try {
            emailService.sendInviteEmail(email, workspace.getName(), inviter.getUsername(), token);
            return true;
        } catch (RuntimeException ex) {
            log.warn("Invite was created but email could not be sent to {}: {}", email, ex.getMessage());
            return false;
        }
    }

    private String buildInviteUrl(String token) {
        String normalizedFrontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;

        return normalizedFrontendUrl + "/accept-invite?token=" + token;
    }

    private WorkspaceResponse map(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .createdBy(workspace.getCreatedBy().getUsername())
                .createdAt(workspace.getCreatedAt())
                .build();
    }
}
