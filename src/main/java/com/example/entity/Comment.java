package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(optional = false)
    private Task task;

    @ManyToOne(optional = false)
    private User user;

    private LocalDateTime createdAt;
}