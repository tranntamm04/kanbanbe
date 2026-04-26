package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String message;

    @ManyToOne
    private Task task;

    @ManyToOne
    private Project project;

    @ManyToOne(optional = false)
    private User user;

    private LocalDateTime createdAt;
}