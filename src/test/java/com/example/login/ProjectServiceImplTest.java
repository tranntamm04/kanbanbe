package com.example.login;

import com.example.dto.project.ProjectRequest;
import com.example.dto.project.ProjectResponse;
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
import com.example.repository.WorkspaceRepository;
import com.example.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMemberRepository memberRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private BoardColumnRepository columnRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private NotificationRepository notificationRepository;

    private ProjectServiceImpl service;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        service = new ProjectServiceImpl(
                projectRepository,
                workspaceRepository,
                memberRepository,
                taskRepository,
                columnRepository,
                commentRepository,
                activityRepository,
                notificationRepository
        );

        workspace = Workspace.builder().id(10L).name("Team").build();
    }

    @Test
    void adminCanCreateProjectWithDefaultColumns() {
        ProjectRequest request = new ProjectRequest();
        request.setName(" Launch Plan ");

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.ADMIN)));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(20L);
            return project;
        });

        ProjectResponse response = service.create(10L, request, 7L);

        assertThat(response.getName()).isEqualTo("Launch Plan");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(response.getWorkspaceId()).isEqualTo(10L);

        ArgumentCaptor<List<BoardColumn>> columnsCaptor = ArgumentCaptor.forClass(List.class);
        verify(columnRepository).saveAll(columnsCaptor.capture());
        assertThat(columnsCaptor.getValue())
                .extracting(BoardColumn::getPosition)
                .containsExactly(0, 1, 2);
        assertThat(columnsCaptor.getValue())
                .extracting(BoardColumn::getName)
                .hasSize(3);
    }

    @Test
    void memberCannotCreateProject() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Member Project");

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.MEMBER)));

        assertThatThrownBy(() -> service.create(10L, request, 7L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Permission denied");

        verify(projectRepository, never()).save(any());
        verify(columnRepository, never()).saveAll(any());
    }

    @Test
    void onlyOwnerCanDeleteProject() {
        Project project = Project.builder().id(20L).workspace(workspace).status(ProjectStatus.ACTIVE).build();

        when(projectRepository.findById(20L)).thenReturn(Optional.of(project));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.ADMIN)));

        assertThatThrownBy(() -> service.delete(20L, 7L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Only owner");
    }

    @Test
    void adminCanArchiveProject() {
        Project project = Project.builder().id(20L).workspace(workspace).status(ProjectStatus.ACTIVE).build();

        when(projectRepository.findById(20L)).thenReturn(Optional.of(project));
        when(memberRepository.findByUserIdAndWorkspaceId(7L, 10L)).thenReturn(Optional.of(member(WorkspaceRole.ADMIN)));

        service.archive(20L, 7L);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
        verify(projectRepository).save(project);
    }

    private WorkspaceMember member(WorkspaceRole role) {
        return WorkspaceMember.builder()
                .user(User.builder().id(7L).username("user").build())
                .workspace(workspace)
                .role(role)
                .build();
    }
}
