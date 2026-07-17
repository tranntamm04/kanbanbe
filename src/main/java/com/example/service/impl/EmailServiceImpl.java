package com.example.service.impl;

import com.example.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("Mã OTP đặt lại mật khẩu");
            helper.setText(
                    """
                    <h3>Mã OTP của bạn là:</h3>
                    <h1 style="color:#2563eb">%s</h1>
                    <p>OTP có hiệu lực trong 15 phút.</p>
                    """.formatted(HtmlUtils.htmlEscape(otp)),
                    true
            );

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Send mail failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendInviteEmail(String toEmail, String workspaceName, String inviterName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String inviteUrl = frontendUrl + "/accept-invite?token=" + token;

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("Lời mời tham gia Workspace: " + workspaceName);
            helper.setText(
                    """
                    <h3>Bạn được mời tham gia Workspace: %s</h3>
                    <p>Người mời: %s</p>
                    <p>Nhấp vào liên kết dưới đây để chấp nhận lời mời:</p>
                    <p><a href="%s">Chấp nhận lời mời</a></p>
                    <p>Liên kết có hiệu lực trong 7 ngày.</p>
                    """.formatted(
                            HtmlUtils.htmlEscape(workspaceName),
                            HtmlUtils.htmlEscape(inviterName),
                            HtmlUtils.htmlEscape(inviteUrl)
                    ),
                    true
            );

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Send mail failed: " + e.getMessage(), e);
        }
    }
}
