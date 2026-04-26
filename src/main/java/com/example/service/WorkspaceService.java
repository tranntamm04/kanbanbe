package com.example.service;

import com.example.dto.workspace.WorkspaceRequest;
import com.example.dto.workspace.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {

    WorkspaceResponse create(WorkspaceRequest request, Long userId);

    List<WorkspaceResponse> getMyWorkspaces(Long userId);

    WorkspaceResponse getById(Long id, Long userId);

    void delete(Long id, Long userId);
}