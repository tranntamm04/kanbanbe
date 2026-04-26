package com.example.service.impl;

import com.example.dto.comment.*;
import com.example.entity.*;
import com.example.exception.AppException;
import com.example.repository.*;
import com.example.service.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final ActivityService activityService;
    private final NotificationService notificationService;

    @Override
    public CommentResponse create(Long taskId, CommentRequest request, Long userId) {

        Task task = getTask(taskId);
        checkMember(task, userId);

        User user = getUser(userId);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        // 🔥 log activity
        activityService.log(
                "COMMENT",
                user.getUsername() + " commented on task",
                task,
                task.getProject(),
                user
        );

        if (task.getAssignee() != null && !task.getAssignee().getId().equals(userId)) {
            notificationService.notify(
                    task.getAssignee(),
                    user.getUsername() + " commented on task \"" + task.getTitle() + "\"",
                    task,
                    task.getProject().getWorkspace()
            );
        }

        return map(comment);
    }

    @Override
    public List<CommentResponse> getByTask(Long taskId, Long userId) {

        Task task = getTask(taskId);
        checkMember(task, userId);

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream().map(this::map).toList();
    }

    @Override
    public void delete(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException("Only owner can delete comment");
        }

        commentRepository.delete(comment);
    }

    // ===== helper =====

    private Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new AppException("Task not found"));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));
    }

    private void checkMember(Task task, Long userId) {
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(
                userId,
                task.getProject().getWorkspace().getId()
        );

        if (!isMember) throw new AppException("Access denied");
    }

    private CommentResponse map(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .taskId(c.getTask().getId())
                .username(c.getUser().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
    }
}