package com.example.dto.workspace;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
}