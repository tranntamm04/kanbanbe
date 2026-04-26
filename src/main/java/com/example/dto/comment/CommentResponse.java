package com.example.dto.comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private Long taskId;
    private String username;
    private LocalDateTime createdAt;
}