package com.example.repository;

import com.example.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);

    void deleteByWorkspaceId(Long workspaceId);

    void deleteByTaskIdIn(List<Long> taskIds);
}