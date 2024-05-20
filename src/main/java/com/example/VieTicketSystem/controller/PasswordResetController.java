package com.example.VieTicketSystem.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.VieTicketSystem.model.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final ObjectMapper mapper;

    // Inject the PasswordResetService and ObjectMapper here
    public PasswordResetController(PasswordResetService passwordResetService, ObjectMapper mapper) {
        this.passwordResetService = passwordResetService;
        this.mapper = mapper;
    }

    @PostMapping("/auth/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Map<String, String> returnedFromService;
        if (email == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", "Email is required");
            return ResponseEntity.badRequest().body(errorNode);
        }

        try {
            // Handle the password reset request
            returnedFromService = passwordResetService.handleFormSubmission(email);
        } catch (Exception e) {
            // Handle the exception here
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorNode);
        }

        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", true);
        successNode.put("message", "OTP sent to " + returnedFromService.get("email"));
        successNode.put("token", returnedFromService.get("token"));
        return ResponseEntity.ok().body(successNode);
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> body) {
        String otp = body.get("otp");
        String email = body.get("email");
        if (otp == null || email == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", "OTP is required");
            return ResponseEntity.badRequest().body(errorNode);
        }

        try {
            // Verify the OTP
            passwordResetService.verifyOTP(email, otp);
        } catch (Exception e) {
            // Handle the exception here
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorNode);
        }

        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", true);
        successNode.put("message", "OTP verified successfully");
        return ResponseEntity.ok().body(successNode);
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || newPassword == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", "Token and new password are required");
            return ResponseEntity.badRequest().body(errorNode);
        }

        try {
            // Reset the password
            passwordResetService.resetPassword(token, newPassword);
        } catch (Exception e) {
            // Handle the exception here
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorNode);
        }

        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", true);
        successNode.put("message", "Password reset successfully");
        return ResponseEntity.ok().body(successNode);
    }
}