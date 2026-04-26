package com.example.service;

import com.example.dto.activity.NotificationResponse;
import com.example.entity.*;

import java.util.List;

public interface NotificationService {

    void notify(User recipient, String message, Task task, Workspace workspace);

    List<NotificationResponse> getMyNotifications(Long userId);

    void markAsRead(Long notificationId, Long userId);
}