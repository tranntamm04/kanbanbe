package com.example.controller;

import com.example.dto.comment.*;
import com.example.service.CommentService;

import com.example.service.CurrentUserService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CurrentUserService currentUserService;
    
    @PostMapping
    public CommentResponse create(@RequestParam Long taskId, @RequestBody CommentRequest request) {
        return commentService.create(taskId, request, currentUserService.get().getId());
    }

    @GetMapping
    public List<CommentResponse> getByTask(@RequestParam Long taskId) {
        return commentService.getByTask(taskId, currentUserService.get().getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        commentService.delete(id, currentUserService.get().getId());
    }
}