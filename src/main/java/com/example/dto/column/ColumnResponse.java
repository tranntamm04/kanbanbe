package com.example.dto.column;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnResponse {
    private Long id;
    private String name;
    private Integer position;
    private Long projectId;
}