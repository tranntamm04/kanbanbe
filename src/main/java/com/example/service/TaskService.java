package com.example.service;

import com.example.dto.task.*;

import java.util.List;

public interface TaskService {

    TaskResponse create(Long columnId, TaskRequest request, Long userId);

    List<TaskResponse> getByColumn(Long columnId, Long userId);

    TaskResponse update(Long taskId, TaskRequest request, Long userId);

    void delete(Long taskId, Long userId);

    TaskResponse move(Long taskId, TaskMoveRequest request, Long userId);
}