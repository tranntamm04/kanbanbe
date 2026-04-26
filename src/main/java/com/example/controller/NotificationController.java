package com.example.controller;

import com.example.dto.activity.NotificationResponse;
import com.example.service.CurrentUserService;
import com.example.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;
    @GetMapping
    public List<NotificationResponse> getMy() {
        return notificationService.getMyNotifications(currentUserService.get().getId());
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, currentUserService.get().getId());
    }
}