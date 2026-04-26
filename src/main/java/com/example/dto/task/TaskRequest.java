package com.example.dto.task;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Long assigneeId;
}