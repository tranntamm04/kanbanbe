package com.example.service;

public interface EmailService {

    void sendOtpEmail(String toEmail, String otp);

    void sendInviteEmail(String toEmail, String workspaceName, String inviterName, String token);
}