package com.example.VieTicketSystem.model.service;

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

    @Value("${OTP_TIME_WINDOW_MINUTES}")
    private String otpTimeWindowMinute;

    public void sendOTP(String email, String username, String otp) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(new InternetAddress(fromEmail, "VinhTicket Account"));
            helper.setTo(email);
            helper.setSubject("VinhTicket - OTP");

            String content = "<h1>VinhTicket Account</h1>"
                    + "<p>Hello " + username + ",</p>"
                    + "<p>Please enter the following One Time Partner (OTP) to access your VinhTicket account.</p>"
                    + "<p style='font-size:18px; font-weight:bold;'>" + otp + "</p>"
                    + "<p>Your OTP remains valid only for " + otpTimeWindowMinute
                    + " minutes.</p>"
                    + "<p>If you did not request this OTP, please ignore this email.</p>"
                    + "<p>In case you forgot your username, it is: <strong>" + username + "</strong></p>"
                    + "<p>Thank you for using VinhTicket!</p>"
                    + "<p>This is an automated email, please do not reply to this email.</p>"
                    + "<br>"
                    + "<p>(c) Copyright (c) 2024 VinhTicket. All rights reserved</p>";

            helper.setText(content, true); // true indicates that the text is HTML

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            // Handle the exception here
            e.printStackTrace();
        }
    }
}
