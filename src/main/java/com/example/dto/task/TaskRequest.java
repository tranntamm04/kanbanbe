package com.example.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {
    @NotBlank
    @Size(max = 160)
    private String title;

    @Size(max = 2000)
    private String description;

    private LocalDateTime dueDate;

    private Long assigneeId;
}
