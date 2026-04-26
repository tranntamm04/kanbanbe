package com.example.service.impl;

import com.example.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("tranductam274@gmail.com");

            helper.setTo(toEmail);

            helper.setSubject("Mã OTP đặt lại mật khẩu");

            helper.setText(
                "<h3>Mã OTP của bạn là:</h3>" +
                "<h1 style='color:blue'>" + otp + "</h1>" +
                "<p>OTP có hiệu lực trong 5 phút</p>",
                true
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Send mail failed: " + e.getMessage());
        }
    }
}