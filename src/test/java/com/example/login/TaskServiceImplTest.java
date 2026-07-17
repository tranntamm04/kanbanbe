package com.example.login;

import com.example.dto.task.TaskRequest;
import com.example.dto.task.TaskResponse;
import com.example.entity.BoardColumn;
import com.example.entity.Project;
import com.example.entity.ProjectStatus;
import com.example.entity.Task;
import com.example.entity.TaskStatus;
import com.example.entity.User;
import com.example.entity.Workspace;
import com.example.entity.WorkspaceMember;
import com.example.entity.WorkspaceRole;
import com.example.exception.AppException;
import com.example.repository.ActivityRepository;
import com.example.repository.BoardColumnRepository;
import com.example.repository.CommentRepository;
import com.example.repository.NotificationRepository;
import com.example.repository.TaskRepository;
import com.example.repository.UserRepository;
import com.example.repository.WorkspaceMemberRepository;
import com.example.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private BoardColumnRepository columnRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkspaceMemberRepository memberRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private NotificationRepository notificationRepository;

    private TaskServiceImpl service;
    private Workspace workspace;
    private Project project;
    private BoardColumn column;
    private User memberUser;
    private User assignee;

    @BeforeEach
    void setUp() {
        service = new TaskServiceImpl(
                taskRepository,
                columnRepository,
                userRepository,
                memberRepository,
                commentRepository,
                activityRepository,
                notificationRepository
        );

        workspace = Workspace.builder().id(10L).name("Team").build();
        project = Project.builder().id(20L).name("Launch").workspace(workspace).status(ProjectStatus.ACTIVE).build();
        column = BoardColumn.builder().id(30L).name("Todo").project(project).position(0).build();
        memberUser = User.builder().id(40L).username("member").email("member@test.dev").build();
        assignee = User.builder().id(41L).username("assignee").email("assignee@test.dev").build();
    }

    @Test
    void memberCanUpdateTaskAssignedToThem() {
        Task task = task(100L, memberUser, assignee);
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated");
        request.setDescription("Better detail");
        request.setAssigneeId(41L);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(memberRepository.findByUserIdAndWorkspaceId(41L, 10L)).thenReturn(Optional.of(member(41L, WorkspaceRole.MEMBER)));
        when(userRepository.findById(41L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = service.update(100L, request, 41L);

        assertThat(response.getTitle()).isEqualTo("Updated");
        assertThat(response.getAssigneeId()).isEqualTo(41L);
        assertThat(response.getCreatedById()).isEqualTo(40L);
    }

    @Test
    void memberCannotUpdateTaskTheyDoNotOwnOrHaveAssigned() {
        User creator = User.builder().id(50L).username("creator").build();
        User otherAssignee = User.builder().id(51L).username("other").build();
        Task task = task(100L, creator, otherAssignee);
        TaskRequest request = new TaskRequest();
        request.setTitle("Nope");

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(memberRepository.findByUserIdAndWorkspaceId(41L, 10L)).thenReturn(Optional.of(member(41L, WorkspaceRole.MEMBER)));

        assertThatThrownBy(() -> service.update(100L, request, 41L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("assigned to you");
    }

    @Test
    void memberCannotDeleteTask() {
        Task task = task(100L, memberUser, memberUser);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(memberRepository.findByUserIdAndWorkspaceId(40L, 10L)).thenReturn(Optional.of(member(40L, WorkspaceRole.MEMBER)));

        assertThatThrownBy(() -> service.delete(100L, 40L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("owners or admins");
    }

    @Test
    void adminCanDeleteTask() {
        Task task = task(100L, memberUser, memberUser);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(memberRepository.findByUserIdAndWorkspaceId(40L, 10L)).thenReturn(Optional.of(member(40L, WorkspaceRole.ADMIN)));

        service.delete(100L, 40L);

        verify(taskRepository).delete(task);
    }

    private WorkspaceMember member(Long userId, WorkspaceRole role) {
        return WorkspaceMember.builder()
                .user(User.builder().id(userId).username("user" + userId).build())
                .workspace(workspace)
                .role(role)
                .build();
    }

    private Task task(Long id, User createdBy, User taskAssignee) {
        return Task.builder()
                .id(id)
                .title("Task")
                .description("Desc")
                .status(TaskStatus.TODO)
                .position(0)
                .project(project)
                .column(column)
                .createdBy(createdBy)
                .assignee(taskAssignee)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
