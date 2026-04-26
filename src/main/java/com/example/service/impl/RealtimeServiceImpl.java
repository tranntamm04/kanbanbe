package com.example.service.impl;

import com.example.dto.RealtimeEvent;
import com.example.service.RealtimeService;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimeServiceImpl implements RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToProject(Long projectId, RealtimeEvent event) {

        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId,
                event
        );
    }

    @Override
    public void sendToUser(Long userId, RealtimeEvent event) {

        messagingTemplate.convertAndSend(
                "/topic/user/" + userId,
                event
        );
    }
}