package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspace_members", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "workspace_id"}))
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    private WorkspaceRole role;
}