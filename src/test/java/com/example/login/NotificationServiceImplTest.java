package com.example.login;

import com.example.dto.activity.NotificationResponse;
import com.example.entity.Notification;
import com.example.entity.Task;
import com.example.entity.User;
import com.example.entity.Workspace;
import com.example.exception.AppException;
import com.example.repository.NotificationRepository;
import com.example.service.NotificationWebSocketBroadcaster;
import com.example.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationWebSocketBroadcaster broadcaster;

    private NotificationServiceImpl service;
    private User recipient;
    private Workspace workspace;
    private Task task;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository, broadcaster);
        recipient = User.builder().id(10L).username("recipient").build();
        workspace = Workspace.builder().id(20L).name("Team").build();
        task = Task.builder().id(30L).title("Task").build();
    }

    @Test
    void notifyPersistsAndBroadcastsNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(40L);
            return notification;
        });

        service.notify(recipient, "Hello", task, workspace);

        verify(notificationRepository).save(any(Notification.class));
        ArgumentCaptor<NotificationResponse> responseCaptor = ArgumentCaptor.forClass(NotificationResponse.class);
        verify(broadcaster).sendToUser(eq(10L), responseCaptor.capture());

        NotificationResponse response = responseCaptor.getValue();
        assertThat(response.getId()).isEqualTo(40L);
        assertThat(response.getMessage()).isEqualTo("Hello");
        assertThat(response.isRead()).isFalse();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getTaskId()).isEqualTo(30L);
        assertThat(response.getWorkspaceId()).isEqualTo(20L);
    }

    @Test
    void getMyNotificationsMapsTaskAndWorkspaceIds() {
        Notification notification = Notification.builder()
                .id(40L)
                .recipient(recipient)
                .message("Hello")
                .task(task)
                .workspace(workspace)
                .isRead(false)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(notification));

        List<NotificationResponse> responses = service.getMyNotifications(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTaskId()).isEqualTo(30L);
        assertThat(responses.get(0).getWorkspaceId()).isEqualTo(20L);
    }

    @Test
    void onlyRecipientCanMarkNotificationAsRead() {
        Notification notification = Notification.builder()
                .id(40L)
                .recipient(recipient)
                .message("Hello")
                .isRead(false)
                .build();

        when(notificationRepository.findById(40L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markAsRead(40L, 99L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void recipientCanMarkNotificationAsRead() {
        Notification notification = Notification.builder()
                .id(40L)
                .recipient(recipient)
                .message("Hello")
                .isRead(false)
                .build();

        when(notificationRepository.findById(40L)).thenReturn(Optional.of(notification));

        service.markAsRead(40L, 10L);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }
}
