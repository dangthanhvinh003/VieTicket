package com.example.VieTicketSystem.service;

import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(new InternetAddress(fromEmail, "VieTicket DO NOT REPLY"));
        } catch (UnsupportedEncodingException e) {
            // Handle the exception here
            e.printStackTrace();
        }

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);

        mailSender.send(message);
    }
}
