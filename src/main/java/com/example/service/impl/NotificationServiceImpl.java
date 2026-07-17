package com.example.service.impl;

import com.example.dto.activity.NotificationResponse;
import com.example.entity.Notification;
import com.example.entity.Task;
import com.example.entity.User;
import com.example.entity.Workspace;
import com.example.exception.AppException;
import com.example.repository.NotificationRepository;
import com.example.service.NotificationService;
import com.example.service.NotificationWebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketBroadcaster webSocketBroadcaster;

    @Override
    public void notify(User recipient, String message, Task task, Workspace workspace) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .task(task)
                .workspace(workspace)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        webSocketBroadcaster.sendToUser(recipient.getId(), map(saved));
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new AppException("Access denied");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponse map(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .taskId(notification.getTask() != null ? notification.getTask().getId() : null)
                .workspaceId(notification.getWorkspace() != null ? notification.getWorkspace().getId() : null)
                .build();
    }
}