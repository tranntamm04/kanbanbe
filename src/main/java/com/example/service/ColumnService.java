package com.example.service;

import com.example.dto.column.ColumnRequest;
import com.example.dto.column.ColumnResponse;

import java.util.List;

public interface ColumnService {

    ColumnResponse create(Long projectId, ColumnRequest request, Long userId);

    List<ColumnResponse> getByProject(Long projectId, Long userId);

    ColumnResponse update(Long columnId, ColumnRequest request, Long userId);

    void delete(Long columnId, Long userId);

    void reorder(Long columnId, Integer newPosition, Long userId);
}