package com.example.dto.workspace;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InviteResponse {
    private String email;
    private String role;
    private String inviteUrl;
    private boolean emailSent;
}
