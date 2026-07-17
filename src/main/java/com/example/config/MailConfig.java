package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${app.mail.host}") String host,
            @Value("${app.mail.port}") int port,
            @Value("${app.mail.username}") String username,
            @Value("${app.mail.password}") String password,
            @Value("${app.mail.protocol:smtp}") String protocol,
            @Value("${app.mail.smtp-auth:true}") String smtpAuth,
            @Value("${app.mail.starttls-enable:true}") String startTlsEnabled,
            @Value("${app.mail.debug:false}") String debug
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", startTlsEnabled);
        props.put("mail.debug", debug);

        return mailSender;
    }
}
