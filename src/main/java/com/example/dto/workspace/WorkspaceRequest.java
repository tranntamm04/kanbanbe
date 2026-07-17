package com.example.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceRequest {
    @NotBlank
    @Size(max = 120)
    private String name;
}
