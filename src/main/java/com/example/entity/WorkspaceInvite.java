package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_invites")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WorkspaceInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @ManyToOne(optional = false)
    private User invitedBy;

    @Enumerated(EnumType.STRING)
    private WorkspaceRole role;

    @Column(unique = true, nullable = false)
    private String token;

    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private boolean accepted;
}