package com.example.service;

import com.example.dto.comment.*;

import java.util.List;

public interface CommentService {

    CommentResponse create(Long taskId, CommentRequest request, Long userId);

    List<CommentResponse> getByTask(Long taskId, Long userId);

    void delete(Long commentId, Long userId);
}