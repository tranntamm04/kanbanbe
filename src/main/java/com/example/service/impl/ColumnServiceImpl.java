package com.example.service.impl;

import com.example.dto.column.*;
import com.example.entity.*;
import com.example.exception.AppException;
import com.example.repository.*;
import com.example.service.ColumnService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColumnServiceImpl implements ColumnService {

    private final BoardColumnRepository columnRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final ActivityRepository activityRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public ColumnResponse create(Long projectId, ColumnRequest request, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found"));

        checkPermission(project, userId);

        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new AppException("Cannot add column to archived project");
        }

        int position = columnRepository.findByProjectIdOrderByPositionAsc(projectId).size();

        BoardColumn column = BoardColumn.builder()
                .name(request.getName())
                .project(project)
                .position(position)
                .build();

        columnRepository.save(column);

        return map(column);
    }

    @Override
    public List<ColumnResponse> getByProject(Long projectId, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found"));

        checkMember(project, userId);

        return columnRepository.findByProjectIdOrderByPositionAsc(projectId)
                .stream().map(this::map).toList();
    }

    @Override
    public ColumnResponse update(Long columnId, ColumnRequest request, Long userId) {

        BoardColumn column = getColumn(columnId);
        checkPermission(column.getProject(), userId);

        column.setName(request.getName());

        return map(columnRepository.save(column));
    }

    @Override
    @Transactional
    public void delete(Long columnId, Long userId) {

        BoardColumn column = getColumn(columnId);
        checkPermission(column.getProject(), userId);

        List<Task> tasks = taskRepository.findByColumnIdOrderByPositionAsc(columnId);
        List<Long> taskIds = tasks.stream().map(Task::getId).toList();

        if (!taskIds.isEmpty()) {
            commentRepository.deleteByTaskIdIn(taskIds);
            activityRepository.deleteByTaskIdIn(taskIds);
            notificationRepository.deleteByTaskIdIn(taskIds);
            taskRepository.deleteByColumnId(columnId);
        }

        columnRepository.delete(column);
    }

    @Override
    public void reorder(Long columnId, Integer newPosition, Long userId) {

        BoardColumn column = getColumn(columnId);
        Project project = column.getProject();

        checkPermission(project, userId);

        List<BoardColumn> columns = columnRepository
                .findByProjectIdOrderByPositionAsc(project.getId());

        if (newPosition == null || newPosition < 0 || newPosition > columns.size() - 1) {
            throw new AppException("Invalid column position");
        }

        columns.remove(column);
        columns.add(newPosition, column);

        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setPosition(i);
        }

        columnRepository.saveAll(columns);
    }

    // ===== HELPER =====

    private BoardColumn getColumn(Long id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new AppException("Column not found"));
    }

    private void checkPermission(Project project, Long userId) {
        WorkspaceMember member = checkMember(project, userId);

        if (member.getRole() == WorkspaceRole.MEMBER) {
            throw new AppException("Permission denied");
        }
    }

    private WorkspaceMember checkMember(Project project, Long userId) {
        return memberRepository
                .findByUserIdAndWorkspaceId(userId, project.getWorkspace().getId())
                .orElseThrow(() -> new AppException("Access denied"));
    }

    private ColumnResponse map(BoardColumn c) {
        return ColumnResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .position(c.getPosition())
                .projectId(c.getProject().getId())
                .build();
    }
}