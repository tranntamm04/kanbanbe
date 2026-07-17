package com.example.service;

import com.example.dto.activity.NotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketRegistry {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessionsByUser.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null) return;

        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId);
        }
    }

    public void sendToUser(Long userId, NotificationResponse notification) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(notification));
            sessions.removeIf((session) -> !send(session, message));
        } catch (IOException ignored) {
            // A malformed payload should not break the request that created the notification.
        }
    }

    private boolean send(WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) return false;

        try {
            session.sendMessage(message);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
