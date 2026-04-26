package com.example.controller;

import com.example.dto.workspace.*;
import com.example.service.CurrentUserService;
import com.example.service.WorkspaceService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final CurrentUserService currentUserService;
    
    @PostMapping
    public WorkspaceResponse create(@RequestBody WorkspaceRequest request) {
        return workspaceService.create(request, currentUserService.get().getId());
    }

    @GetMapping
    public List<WorkspaceResponse> getMy() {
        return workspaceService.getMyWorkspaces(currentUserService.get().getId());
    }

    @GetMapping("/{id}")
    public WorkspaceResponse get(@PathVariable Long id) {
        return workspaceService.getById(id, currentUserService.get().getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        workspaceService.delete(id, currentUserService.get().getId());
    }
}