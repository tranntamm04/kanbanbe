package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspaces")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    private User createdBy;

    private LocalDateTime createdAt;
}