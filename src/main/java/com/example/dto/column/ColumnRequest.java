package com.example.dto.column;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ColumnRequest {
    @NotBlank
    @Size(max = 80)
    private String name;
}
