package com.example.VieTicketSystem.controller;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.VieTicketSystem.model.service.PasswordResetService;

@RestController
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // Inject the PasswordResetService here
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/auth/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            // Handle the password reset request
            passwordResetService.handleFormSubmission(email);
        } catch (Exception e) {
            // Handle the exception here
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.ok().body("Please check your email for the OTP");
    }
}