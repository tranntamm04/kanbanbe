package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(MyConstants.MAIL_HOST);
        mailSender.setPort(MyConstants.MAIL_PORT);
        mailSender.setUsername(MyConstants.MAIL_USERNAME);
        mailSender.setPassword(MyConstants.MAIL_PASSWORD);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", MyConstants.MAIL_PROTOCOL);
        props.put("mail.smtp.auth", MyConstants.MAIL_SMTP_AUTH);
        props.put("mail.smtp.starttls.enable", MyConstants.MAIL_SMTP_STARTTLS_ENABLE);
        props.put("mail.debug", "true");

        return mailSender;
    }
}
