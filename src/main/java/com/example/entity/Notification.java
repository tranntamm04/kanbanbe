package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User recipient;

    private String type;

    private String message;

    @ManyToOne
    private Task task;

    @ManyToOne
    private Workspace workspace;

    private boolean isRead;

    private LocalDateTime createdAt;
}