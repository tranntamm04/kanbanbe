package com.example.dto.project;

import com.example.entity.ProjectStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private ProjectStatus status;
    private Long workspaceId;
}