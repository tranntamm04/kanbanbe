package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime dueDate;

    private Integer position;

    @ManyToOne(optional = false)
    private Project project;

    @ManyToOne(optional = false)
    private BoardColumn column;

    @ManyToOne
    private User assignee;

    @ManyToOne(optional = false)
    private User createdBy;

    private LocalDateTime createdAt;
}