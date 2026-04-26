package com.example.service.impl;

import com.example.dto.activity.NotificationResponse;
import com.example.entity.*;
import com.example.exception.AppException;
import com.example.repository.NotificationRepository;
import com.example.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

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

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Long userId) {

        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {

        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException("Notification not found"));

        if (!n.getRecipient().getId().equals(userId)) {
            throw new AppException("Access denied");
        }

        n.setRead(true);
        notificationRepository.save(n);
    }
}