package com.example.service;

import com.example.dto.RealtimeEvent;

public interface RealtimeService {

    void sendToProject(Long projectId, RealtimeEvent event);

    void sendToUser(Long userId, RealtimeEvent event);
}