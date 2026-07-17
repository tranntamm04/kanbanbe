package com.example.dto.workspace;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InviteRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "OWNER|ADMIN|MEMBER", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String role;
}
