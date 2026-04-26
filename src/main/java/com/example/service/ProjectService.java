package com.example.service;

import com.example.dto.project.ProjectRequest;
import com.example.dto.project.ProjectResponse;

import java.util.List;

public interface ProjectService {

    ProjectResponse create(Long workspaceId, ProjectRequest request, Long userId);

    List<ProjectResponse> getByWorkspace(Long workspaceId, Long userId);

    ProjectResponse getById(Long projectId, Long userId);

    ProjectResponse update(Long projectId, ProjectRequest request, Long userId);

    void delete(Long projectId, Long userId);

    void archive(Long projectId, Long userId);
}