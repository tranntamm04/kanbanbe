package com.example.service.impl;

import com.example.dto.activity.ActivityResponse;
import com.example.entity.*;
import com.example.repository.*;
import com.example.service.ActivityService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository memberRepository;

    @Override
    public void log(String type, String message, Task task, Project project, User user) {

        Activity activity = Activity.builder()
                .type(type)
                .message(message)
                .task(task)
                .project(project)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        activityRepository.save(activity);
    }

    @Override
    public List<ActivityResponse> getByTask(Long taskId, Long userId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow();

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(
                userId,
                task.getProject().getWorkspace().getId()
        );

        if (!isMember) throw new RuntimeException("Access denied");

        return activityRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(a -> ActivityResponse.builder()
                        .type(a.getType())
                        .message(a.getMessage())
                        .username(a.getUser().getUsername())
                        .taskId(taskId)
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }
}