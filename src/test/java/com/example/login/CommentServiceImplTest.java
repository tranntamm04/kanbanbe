package com.example.login;

import com.example.dto.comment.CommentRequest;
import com.example.dto.comment.CommentResponse;
import com.example.entity.Comment;
import com.example.entity.Project;
import com.example.entity.ProjectStatus;
import com.example.entity.Task;
import com.example.entity.TaskStatus;
import com.example.entity.User;
import com.example.entity.Workspace;
import com.example.exception.AppException;
import com.example.repository.CommentRepository;
import com.example.repository.TaskRepository;
import com.example.repository.UserRepository;
import com.example.repository.WorkspaceMemberRepository;
import com.example.service.ActivityService;
import com.example.service.NotificationService;
import com.example.service.impl.CommentServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock private CommentRepository commentRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkspaceMemberRepository memberRepository;
    @Mock private ActivityService activityService;
    @Mock private NotificationService notificationService;

    private CommentServiceImpl service;
    private Workspace workspace;
    private Project project;
    private Task task;
    private User author;
    private User assignee;

    @BeforeEach
    void setUp() {
        service = new CommentServiceImpl(
                commentRepository,
                taskRepository,
                userRepository,
                memberRepository,
                activityService,
                notificationService
        );

        workspace = Workspace.builder().id(10L).name("Team").build();
        project = Project.builder().id(20L).name("Project").workspace(workspace).status(ProjectStatus.ACTIVE).build();
        author = User.builder().id(30L).username("author").build();
        assignee = User.builder().id(31L).username("assignee").build();
        task = Task.builder()
                .id(40L)
                .title("Important")
                .project(project)
                .status(TaskStatus.TODO)
                .assignee(assignee)
                .createdBy(author)
                .build();
    }

    @Test
    void memberCanCreateCommentAndNotifyDifferentAssignee() {
        CommentRequest request = new CommentRequest();
        request.setContent("Please check");

        when(taskRepository.findById(40L)).thenReturn(Optional.of(task));
        when(memberRepository.existsByUserIdAndWorkspaceId(30L, 10L)).thenReturn(true);
        when(userRepository.findById(30L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(50L);
            comment.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
            return comment;
        });

        CommentResponse response = service.create(40L, request, 30L);

        assertThat(response.getContent()).isEqualTo("Please check");
        assertThat(response.getUsername()).isEqualTo("author");
        verify(activityService).log(eq("COMMENT"), eq("author commented on task"), eq(task), eq(project), eq(author));
        verify(notificationService).notify(eq(assignee), eq("author commented on task \"Important\""), eq(task), eq(workspace));
    }

    @Test
    void commentDoesNotNotifyWhenAuthorIsAssignee() {
        task.setAssignee(author);
        CommentRequest request = new CommentRequest();
        request.setContent("Self update");

        when(taskRepository.findById(40L)).thenReturn(Optional.of(task));
        when(memberRepository.existsByUserIdAndWorkspaceId(30L, 10L)).thenReturn(true);
        when(userRepository.findById(30L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(40L, request, 30L);

        verify(notificationService, never()).notify(any(), any(), any(), any());
    }

    @Test
    void nonMemberCannotCreateComment() {
        CommentRequest request = new CommentRequest();
        request.setContent("No access");

        when(taskRepository.findById(40L)).thenReturn(Optional.of(task));
        when(memberRepository.existsByUserIdAndWorkspaceId(99L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(40L, request, 99L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void onlyCommentOwnerCanDeleteComment() {
        Comment comment = Comment.builder().id(50L).user(author).task(task).content("Hello").build();

        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.delete(50L, 31L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Only owner");
    }

    @Test
    void ownerCanDeleteComment() {
        Comment comment = Comment.builder().id(50L).user(author).task(task).content("Hello").build();

        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

        service.delete(50L, 30L);

        verify(commentRepository).delete(comment);
    }
}

