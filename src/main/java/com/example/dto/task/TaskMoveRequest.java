package com.example.dto.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TaskMoveRequest {

    @NotNull
    @Positive
    private Long targetColumnId;

    @PositiveOrZero
    private Integer newPosition;
}
