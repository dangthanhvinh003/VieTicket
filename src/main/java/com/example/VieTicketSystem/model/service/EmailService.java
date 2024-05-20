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

            String content = "<html><body style='font-family: sans-serif;'>" +
                    "<h1 style='color: #007bff;'>VinhTicket Account</h1>" +
                    "<p style='font-size: 16px;'>Hello " + username + ",</p>" +
                    "<p style='font-size: 16px;'>Please enter the following One Time Partner (OTP) to access your VinhTicket account.</p>"
                    +
                    "<p style='font-size: 24px; font-weight: bold; color: #f00;'>" + otp + "</p>" +
                    "<p style='font-size: 16px;'>Your OTP remains valid only for " + otpTimeWindowMinute
                    + " minutes.</p>" +
                    "<p style='font-size: 16px;'>If you did not request this OTP, please ignore this email.</p>" +
                    "<p style='font-size: 16px;'>In case you forgot your username, it is: <strong style='color: #333;'>"
                    + username + "</strong></p>" +
                    "<p style='font-size: 16px;'>Thank you for using VinhTicket!</p>" +
                    "<p style='font-size: 14px; color: #777;'>This is an automated email, please do not reply to this email.</p>"
                    +
                    "<br>" +
                    "<p style='font-size: 12px; color: #555;'>(c) Copyright (c) 2024 VinhTicket. All rights reserved</p>"
                    +
                    "</body></html>";

            helper.setText(content, true); // true indicates that the text is HTML

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            // Handle the exception here
            e.printStackTrace();
        }
    }
}
