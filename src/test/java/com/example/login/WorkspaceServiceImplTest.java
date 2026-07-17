package com.example.login;

import com.example.dto.workspace.InviteRequest;
import com.example.dto.workspace.InviteResponse;
import com.example.entity.User;
import com.example.entity.Workspace;
import com.example.entity.WorkspaceMember;
import com.example.entity.WorkspaceRole;
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
import com.example.service.impl.WorkspaceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private WorkspaceInviteRepository inviteRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private BoardColumnRepository columnRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private EmailService emailService;
    @Mock private NotificationService notificationService;

    private WorkspaceServiceImpl service;
    private Workspace workspace;
    private User inviter;

    @BeforeEach
    void setUp() {
        service = new WorkspaceServiceImpl(
                workspaceRepository,
                memberRepository,
                userRepository,
                taskRepository,
                inviteRepository,
                projectRepository,
                columnRepository,
                commentRepository,
                activityRepository,
                notificationRepository,
                emailService,
                notificationService
        );
        ReflectionTestUtils.setField(service, "frontendUrl", "https://kanban.example.com/");

        inviter = User.builder().id(1L).username("owner").email("owner@test.dev").build();
        workspace = Workspace.builder().id(10L).name("Product").createdBy(inviter).build();
    }

    @Test
    void inviteStillReturnsLinkWhenEmailProviderFails() {
        InviteRequest request = new InviteRequest();
        request.setEmail(" NewUser@Test.Dev ");
        request.setRole("MEMBER");

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(memberRepository.findByUserIdAndWorkspaceId(1L, 10L)).thenReturn(Optional.of(ownerMember()));
        when(userRepository.findByEmail("newuser@test.dev")).thenReturn(Optional.empty());
        when(inviteRepository.findByEmailAndWorkspaceId("newuser@test.dev", 10L)).thenReturn(Optional.empty());
        when(inviteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailService)
                .sendInviteEmail(eq("newuser@test.dev"), eq("Product"), eq("owner"), any());

        InviteResponse response = service.inviteUser(10L, request, 1L);

        assertThat(response.isEmailSent()).isFalse();
        assertThat(response.getEmail()).isEqualTo("newuser@test.dev");
        assertThat(response.getRole()).isEqualTo("MEMBER");
        assertThat(response.getInviteUrl()).startsWith("https://kanban.example.com/accept-invite?token=");
        verify(inviteRepository).save(any());
    }

    private WorkspaceMember ownerMember() {
        return WorkspaceMember.builder()
                .user(inviter)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .build();
    }
}
