package com.example.dto.workspace;

import lombok.Data;

@Data
public class InviteRequest {
    private String email;
    private String role; // OWNER, ADMIN, MEMBER
}