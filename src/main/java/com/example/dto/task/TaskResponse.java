package com.example.dto.task;

import com.example.entity.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer position;
    private Long columnId;
    private Long assigneeId;
    private LocalDateTime dueDate;
}