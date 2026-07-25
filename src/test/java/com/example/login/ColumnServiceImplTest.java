package com.example.login;

import com.example.dto.column.ColumnRequest;
import com.example.dto.column.ColumnResponse;
import com.example.entity.BoardColumn;
import com.example.entity.Project;
import com.example.entity.ProjectStatus;
import com.example.entity.User;
import com.example.entity.Workspace;
import com.example.entity.WorkspaceMember;
import com.example.entity.WorkspaceRole;
import com.example.exception.AppException;
import com.example.repository.ActivityRepository;
import com.example.repository.BoardColumnRepository;
import com.example.repository.CommentRepository;
import com.example.repository.NotificationRepository;
import com.example.repository.ProjectRepository;
import com.example.repository.TaskRepository;
import com.example.repository.WorkspaceMemberRepository;
import com.example.service.impl.ColumnServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColumnServiceImplTest {

    @Mock private BoardColumnRepository columnRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private WorkspaceMemberRepository memberRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private NotificationRepository notificationRepository;

    private ColumnServiceImpl service;
    private Workspace workspace;
    private Project project;

    @BeforeEach
    void setUp() {
        service = new ColumnServiceImpl(
                columnRepository,
                projectRepository,
                memberRepository,
                taskRepository,
                commentRepository,
                activityRepository,
                notificationRepository
        );

        workspace = Workspace.builder().id(10L).name("Team").build();
        project = Project.builder().id(20L).name("Board").workspace(workspace).status(ProjectStatus.ACTIVE).build();
    }

    @Test
    void adminCanCreateColumnAtEnd() {
        ColumnRequest request = new ColumnRequest();
        request.setName("Review");

        when(projectRepository.findById(20L)).thenReturn(Optional.of(project));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.ADMIN)));
        when(columnRepository.countByProjectId(20L)).thenReturn(2L);
        when(columnRepository.save(any(BoardColumn.class))).thenAnswer(invocation -> {
            BoardColumn column = invocation.getArgument(0);
            column.setId(30L);
            return column;
        });

        ColumnResponse response = service.create(20L, request, 7L);

        assertThat(response.getName()).isEqualTo("Review");
        assertThat(response.getPosition()).isEqualTo(2);
        assertThat(response.getProjectId()).isEqualTo(20L);
    }

    @Test
    void memberCannotCreateColumn() {
        ColumnRequest request = new ColumnRequest();
        request.setName("Blocked");

        when(projectRepository.findById(20L)).thenReturn(Optional.of(project));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.MEMBER)));

        assertThatThrownBy(() -> service.create(20L, request, 7L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Permission denied");

        verify(columnRepository, never()).save(any());
    }

    @Test
    void cannotCreateColumnInArchivedProject() {
        project.setStatus(ProjectStatus.ARCHIVED);
        ColumnRequest request = new ColumnRequest();
        request.setName("Blocked");

        when(projectRepository.findById(20L)).thenReturn(Optional.of(project));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.ADMIN)));

        assertThatThrownBy(() -> service.create(20L, request, 7L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("archived project");
    }

    @Test
    void reorderColumnRecalculatesPositions() {
        BoardColumn todo = column(1L, "Todo", 0);
        BoardColumn doing = column(2L, "Doing", 1);
        BoardColumn done = column(3L, "Done", 2);
        List<BoardColumn> columns = new ArrayList<>(List.of(todo, doing, done));

        when(columnRepository.findById(3L)).thenReturn(Optional.of(done));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.ADMIN)));
        when(columnRepository.findByProjectIdOrderByPositionAsc(20L)).thenReturn(columns);

        service.reorder(3L, 0, 7L);

        assertThat(done.getPosition()).isEqualTo(0);
        assertThat(todo.getPosition()).isEqualTo(1);
        assertThat(doing.getPosition()).isEqualTo(2);
        verify(columnRepository).saveAll(columns);
    }

    @Test
    void reorderRejectsInvalidPosition() {
        BoardColumn todo = column(1L, "Todo", 0);
        List<BoardColumn> columns = new ArrayList<>(List.of(todo));

        when(columnRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.ADMIN)));
        when(columnRepository.findByProjectIdOrderByPositionAsc(20L)).thenReturn(columns);

        assertThatThrownBy(() -> service.reorder(1L, 3, 7L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid column position");
    }

    private BoardColumn column(Long id, String name, int position) {
        return BoardColumn.builder().id(id).name(name).project(project).position(position).build();
    }

    private WorkspaceMember member(WorkspaceRole role) {
        return WorkspaceMember.builder()
                .user(User.builder().id(7L).username("user").build())
                .workspace(workspace)
                .role(role)
                .build();
    }
}
