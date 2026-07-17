package com.example.service;

import com.example.dto.activity.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketBroadcaster {

    private final NotificationWebSocketRegistry registry;

    public void sendToUser(Long userId, NotificationResponse notification) {
        registry.sendToUser(userId, notification);
    }
}
