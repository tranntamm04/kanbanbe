package com.example.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "projects")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @ManyToOne(optional = false)
    private Workspace workspace;
}