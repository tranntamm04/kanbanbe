package com.example.dto.activity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityResponse {

    private String type;
    private String message;
    private String username;
    private Long taskId;
    private LocalDateTime createdAt;
}