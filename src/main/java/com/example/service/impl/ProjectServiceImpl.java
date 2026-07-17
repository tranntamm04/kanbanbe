package com.example.service.impl;

import com.example.dto.project.ProjectRequest;
import com.example.dto.project.ProjectResponse;
import com.example.entity.*;
import com.example.exception.AppException;
import com.example.repository.*;
import com.example.service.ProjectService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final List<String> DEFAULT_COLUMN_NAMES = List.of("Cần làm", "Đang làm", "Hoàn thành");

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final BoardColumnRepository columnRepository;
    private final CommentRepository commentRepository;
    private final ActivityRepository activityRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public ProjectResponse create(Long workspaceId, ProjectRequest request, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new AppException("Workspace not found"));

        WorkspaceMember member = memberRepository
                .findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new AppException("Not a member"));

        if (member.getRole() == WorkspaceRole.MEMBER) {
            throw new AppException("Permission denied");
        }

        Project project = Project.builder()
                .name(request.getName().trim())
                .workspace(workspace)
                .status(ProjectStatus.ACTIVE)
                .build();

        projectRepository.save(project);
        createDefaultColumns(project);

        return map(project);
    }

    @Override
    public List<ProjectResponse> getByWorkspace(Long workspaceId, Long userId) {
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);

        if (!isMember) {
            throw new AppException("Access denied");
        }

        return projectRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public ProjectResponse getById(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found"));

        checkMember(project.getWorkspace().getId(), userId);

        return map(project);
    }

    @Override
    public ProjectResponse update(Long projectId, ProjectRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found"));

        WorkspaceMember member = checkMember(project.getWorkspace().getId(), userId);

        if (member.getRole() == WorkspaceRole.MEMBER) {
            throw new AppException("Permission denied");
        }

        project.setName(request.getName().trim());

        return map(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found"));

        WorkspaceMember member = checkMember(project.getWorkspace().getId(), userId);

        if (member.getRole() != WorkspaceRole.OWNER) {
            throw new AppException("Only owner can delete project");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<Long> taskIds = tasks.stream().map(Task::getId).toList();

        if (!taskIds.isEmpty()) {
            commentRepository.deleteByTaskIdIn(taskIds);
            activityRepository.deleteByTaskIdIn(taskIds);
            notificationRepository.deleteByTaskIdIn(taskIds);
            taskRepository.deleteByProjectId(projectId);
        }

        activityRepository.deleteByProjectId(projectId);
        columnRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
    }

    @Override
    public void archive(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found"));

        WorkspaceMember member = checkMember(project.getWorkspace().getId(), userId);

        if (member.getRole() == WorkspaceRole.MEMBER) {
            throw new AppException("Permission denied");
        }

        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }

    private WorkspaceMember checkMember(Long workspaceId, Long userId) {
        return memberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new AppException("Access denied"));
    }

    private void createDefaultColumns(Project project) {
        List<BoardColumn> columns = DEFAULT_COLUMN_NAMES.stream()
                .map(name -> BoardColumn.builder()
                        .name(name)
                        .project(project)
                        .position(DEFAULT_COLUMN_NAMES.indexOf(name))
                        .build())
                .toList();

        columnRepository.saveAll(columns);
    }

    private ProjectResponse map(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .workspaceId(p.getWorkspace().getId())
                .build();
    }
}
