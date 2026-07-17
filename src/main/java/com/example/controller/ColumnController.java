package com.example.controller;

import com.example.dto.column.*;
import com.example.service.ColumnService;

import com.example.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/columns")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ColumnResponse create(
            @RequestParam Long projectId,
            @Valid @RequestBody ColumnRequest request
    ) {
        return columnService.create(projectId, request, currentUserService.get().getId());
    }

    @GetMapping
    public List<ColumnResponse> getByProject(@RequestParam Long projectId) {
        return columnService.getByProject(projectId, currentUserService.get().getId());
    }

    @PutMapping("/{id}")
    public ColumnResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ColumnRequest request
    ) {
        return columnService.update(id, request, currentUserService.get().getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        columnService.delete(id, currentUserService.get().getId());
    }

    @PutMapping("/{id}/reorder")
    public void reorder(@PathVariable Long id, @RequestParam Integer position) {
        columnService.reorder(id, position, currentUserService.get().getId());
    }
}
