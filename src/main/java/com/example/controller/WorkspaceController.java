package com.example.controller;

import com.example.dto.workspace.InviteRequest;
import com.example.dto.workspace.InviteResponse;
import com.example.dto.workspace.WorkspaceMemberResponse;
import com.example.dto.workspace.WorkspaceRequest;
import com.example.dto.workspace.WorkspaceResponse;
import com.example.service.CurrentUserService;
import com.example.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public WorkspaceResponse create(@Valid @RequestBody WorkspaceRequest request) {
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

    @PostMapping("/{id}/invite")
    public InviteResponse invite(@PathVariable Long id, @Valid @RequestBody InviteRequest request) {
        return workspaceService.inviteUser(id, request, currentUserService.get().getId());
    }

    @GetMapping("/{id}/members")
    public List<WorkspaceMemberResponse> getMembers(@PathVariable Long id) {
        return workspaceService.getMembers(id, currentUserService.get().getId());
    }
}
