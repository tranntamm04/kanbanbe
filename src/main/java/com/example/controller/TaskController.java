package com.example.controller;

import com.example.dto.task.*;
import com.example.service.CurrentUserService;
import com.example.service.TaskService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserService currentUserService;
    
    @PostMapping
    public TaskResponse create(
            @RequestParam Long columnId,
            @RequestBody TaskRequest request
    ) {
        return taskService.create(columnId, request, currentUserService.get().getId());
    }

    @GetMapping
    public List<TaskResponse> getByColumn(@RequestParam Long columnId) {
        return taskService.getByColumn(columnId, currentUserService.get().getId());
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable Long id,
            @RequestBody TaskRequest request
    ) {
        return taskService.update(id, request, currentUserService.get().getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskService.delete(id, currentUserService.get().getId());
    }

    @PutMapping("/{id}/move")
    public TaskResponse move(
            @PathVariable Long id,
            @RequestBody TaskMoveRequest request
    ) {
        return taskService.move(id, request, currentUserService.get().getId());
    }
}