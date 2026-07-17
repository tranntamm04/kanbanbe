package com.example.service.impl;

import com.example.dto.task.*;
import com.example.entity.*;
import com.example.exception.AppException;
import com.example.repository.*;
import com.example.service.TaskService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final ActivityRepository activityRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public TaskResponse create(Long columnId, TaskRequest request, Long userId) {

        BoardColumn column = getColumn(columnId);
        requireWorkspaceMember(column, userId);
        ensureProjectIsEditable(column.getProject(), "Cannot add task to archived project");

        int position = Math.toIntExact(taskRepository.countByColumnId(columnId));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .column(column)
                .project(column.getProject())
                .position(position)
                .status(TaskStatus.TODO)
                .createdBy(getUser(userId))
                .createdAt(LocalDateTime.now())
                .assignee(getAssignee(request.getAssigneeId()))
                .build();

        taskRepository.save(task);

        return map(task);
    }

    @Override
    public List<TaskResponse> getByColumn(Long columnId, Long userId) {

        BoardColumn column = getColumn(columnId);
        requireWorkspaceMember(column, userId);

        return taskRepository.findByColumnIdOrderByPositionAsc(columnId)
                .stream().map(this::map).toList();
    }

    @Override
    public TaskResponse update(Long taskId, TaskRequest request, Long userId) {

        Task task = getTask(taskId);
        requireCanEditTask(task, userId);
        ensureProjectIsEditable(task.getProject(), "Cannot edit tasks in archived project");

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setAssignee(getAssignee(request.getAssigneeId()));

        return map(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void delete(Long taskId, Long userId) {

        Task task = getTask(taskId);
        requireBoardManager(task.getColumn(), userId);
        ensureProjectIsEditable(task.getProject(), "Cannot delete tasks in archived project");

        commentRepository.deleteByTaskIdIn(List.of(taskId));
        activityRepository.deleteByTaskIdIn(List.of(taskId));
        notificationRepository.deleteByTaskIdIn(List.of(taskId));

        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse move(Long taskId, TaskMoveRequest request, Long userId) {

        Task task = getTask(taskId);
        BoardColumn sourceColumn = task.getColumn();
        BoardColumn targetColumn = getColumn(request.getTargetColumnId());

        requireCanEditTask(task, userId);
        requireSameProject(sourceColumn, targetColumn);
        ensureProjectIsEditable(task.getProject(), "Cannot move tasks inside archived project");

        List<Task> sourceTasks = taskRepository.findByColumnIdOrderByPositionAsc(sourceColumn.getId());
        List<Task> targetTasks = taskRepository.findByColumnIdOrderByPositionAsc(targetColumn.getId());

        int newPosition = request.getNewPosition() == null ? targetTasks.size() : request.getNewPosition();
        int maxPosition = sourceColumn.getId().equals(targetColumn.getId())
                ? targetTasks.size() - 1
                : targetTasks.size();

        if (newPosition < 0 || newPosition > maxPosition) {
            throw new AppException("Invalid task position");
        }

        sourceTasks.remove(task);

        if (!sourceColumn.getId().equals(targetColumn.getId())) {
            task.setColumn(targetColumn);
            targetTasks.add(newPosition, task);

            for (int i = 0; i < sourceTasks.size(); i++) {
                sourceTasks.get(i).setPosition(i);
            }
            for (int i = 0; i < targetTasks.size(); i++) {
                targetTasks.get(i).setPosition(i);
            }

            taskRepository.saveAll(sourceTasks);
            taskRepository.saveAll(targetTasks);
        } else {
            targetTasks = sourceTasks;
            targetTasks.add(newPosition, task);

            for (int i = 0; i < targetTasks.size(); i++) {
                targetTasks.get(i).setPosition(i);
            }

            taskRepository.saveAll(targetTasks);
        }

        return map(task);
    }

    private Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new AppException("Task not found"));
    }

    private BoardColumn getColumn(Long id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new AppException("Column not found"));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));
    }

    private User getAssignee(Long id) {
        if (id == null) return null;
        return getUser(id);
    }

    private WorkspaceMember requireWorkspaceMember(BoardColumn column, Long userId) {
        Long workspaceId = column.getProject().getWorkspace().getId();
        return memberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new AppException("Access denied"));
    }

    private void requireBoardManager(BoardColumn column, Long userId) {
        WorkspaceMember member = requireWorkspaceMember(column, userId);
        if (!isBoardManager(member)) {
            throw new AppException("Only workspace owners or admins can manage this board");
        }
    }

    private void requireCanEditTask(Task task, Long userId) {
        WorkspaceMember member = requireWorkspaceMember(task.getColumn(), userId);
        if (isBoardManager(member) || isCreatedBy(task, userId) || isAssignedTo(task, userId)) {
            return;
        }

        throw new AppException("You can only edit tasks assigned to you or created by you");
    }

    private boolean isBoardManager(WorkspaceMember member) {
        return member.getRole() == WorkspaceRole.OWNER || member.getRole() == WorkspaceRole.ADMIN;
    }

    private boolean isCreatedBy(Task task, Long userId) {
        return task.getCreatedBy() != null && task.getCreatedBy().getId().equals(userId);
    }

    private boolean isAssignedTo(Task task, Long userId) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(userId);
    }

    private void ensureProjectIsEditable(Project project, String message) {
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new AppException(message);
        }
    }

    private void requireSameProject(BoardColumn sourceColumn, BoardColumn targetColumn) {
        if (!sourceColumn.getProject().getId().equals(targetColumn.getProject().getId())) {
            throw new AppException("Cannot move task to another project");
        }
    }

    private TaskResponse map(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .position(t.getPosition())
                .columnId(t.getColumn().getId())
                .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                .createdById(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null)
                .dueDate(t.getDueDate())
                .build();
    }
}
