package com.example.dto.task;

import lombok.Data;

@Data
public class TaskMoveRequest {

    private Long targetColumnId;
    private Integer newPosition;
}
