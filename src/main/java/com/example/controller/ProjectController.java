package com.example.controller;

import com.example.dto.project.*;
import com.example.service.CurrentUserService;
import com.example.service.ProjectService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final CurrentUserService currentUserService;
    
    @PostMapping
    public ProjectResponse create(@RequestParam Long workspaceId, @RequestBody ProjectRequest request) {
        return projectService.create(workspaceId, request, currentUserService.get().getId());
    }

    @GetMapping
    public List<ProjectResponse> getByWorkspace(@RequestParam Long workspaceId) {
        return projectService.getByWorkspace(workspaceId, currentUserService.get().getId());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        return projectService.getById(id, currentUserService.get().getId());
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectRequest request) {
        return projectService.update(id, request, currentUserService.get().getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectService.delete(id, currentUserService.get().getId());
    }

    @PutMapping("/{id}/archive")
    public void archive(@PathVariable Long id) {
        projectService.archive(id, currentUserService.get().getId());
    }
}