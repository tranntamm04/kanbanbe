package com.example.dto.workspace;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String createdBy;
    private LocalDateTime createdAt;
}