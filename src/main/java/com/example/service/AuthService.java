package com.example.service;

import com.example.dto.*;

public interface AuthService {

    void register(RegisterRequest request);

    JwtResponse login(LoginRequest request);

    void forgotPassword(String email);

    void resetPassword(String email, String otp, String newPassword);
}