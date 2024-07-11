package com.example.VieTicketSystem.controller;

import java.util.Map;

import com.example.VieTicketSystem.model.entity.User;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.VieTicketSystem.service.PasswordResetService;
import com.example.VieTicketSystem.service.VerifyEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth/")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final ObjectMapper mapper;
    private final VerifyEmailService verifyEmailService;
    private final HttpServletResponse httpServletResponse;
    private final HttpSession httpSession;

    // Inject the PasswordResetService and ObjectMapper here
    public PasswordResetController(PasswordResetService passwordResetService, ObjectMapper mapper,
                                   VerifyEmailService verifyEmailService, HttpServletResponse httpServletResponse, HttpSession httpSession) {
        this.passwordResetService = passwordResetService;
        this.mapper = mapper;
        this.verifyEmailService = verifyEmailService;
        this.httpServletResponse = httpServletResponse;
        this.httpSession = httpSession;
    }

    @PostMapping("/password-reset/request-reset")
    public ResponseEntity<?> passwordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String returnedFromService;
        if (email == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", "Email is required");
            return ResponseEntity.badRequest().body(errorNode);
        }

        try {
            // Handle the password reset request
            if (verifyEmailService.isUnverified(email)) {
                returnedFromService = verifyEmailService.sendOTP(email);
            } else {
                returnedFromService = passwordResetService.handleFormSubmission(email);
            }
        } catch (Exception e) {
            // Handle the exception here
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", e.getLocalizedMessage());
            // errorNode.put("message",
            // "An error occurred while processing the request. Please recheck your input
            // and try again later.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorNode);
        }

        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", true);
        successNode.put("message", returnedFromService);
        return ResponseEntity.ok().body(successNode);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> body) {

        String otp = body.get("otp");
        String email = body.get("email");
        if (otp == null || email == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", "OTP is required");
            return ResponseEntity.badRequest().body(errorNode);
        }
        String resetToken = null;
        try {
            // Verify the OTP
            if (verifyEmailService.isUnverified(email)) {
                verifyEmailService.verifyOTP(email, otp);
                User user = (User) httpSession.getAttribute("activeUser");
                user.setRole(Character.toLowerCase(user.getRole()));
            } else {
                resetToken = passwordResetService.verifyOTP(email, otp);
            }

            // Create a cookie
            Cookie cookie = new Cookie("token", resetToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // ensures the cookie is only sent over HTTPS TODO: enable this in production
            cookie.setPath("/"); // accessible to entire domain

            // Add cookie to response
            httpServletResponse.addCookie(cookie);
        } catch (Exception e) {
            // Handle the exception here
            e.printStackTrace();
            if (e instanceof ResponseStatusException) {
                ObjectNode errorNode = mapper.createObjectNode();
                errorNode.put("success", false);
                errorNode.put("message", ((ResponseStatusException) e).getReason());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorNode);
            } else {
                ObjectNode errorNode = mapper.createObjectNode();
                errorNode.put("success", false);
                errorNode.put("message", "Internal server error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorNode);
            }
        }

        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", true);
        successNode.put("message", "OTP verified successfully");
        return ResponseEntity.ok().body(successNode);
    }

    @PostMapping("/password-reset/new-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body, @CookieValue(value = "token", defaultValue = "") String token) {

        String newPassword = body.get("newPassword");

        if ("".equals(token) || newPassword == null) {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", "Token and new password are required");
            return ResponseEntity.badRequest().body(errorNode);
        }

        try {
            // Reset the password
            passwordResetService.resetPassword(token, newPassword);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                ObjectNode errorNode = mapper.createObjectNode();
                errorNode.put("success", false);
                errorNode.put("message", ((ResponseStatusException) e).getReason());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorNode);
            }

            // Handle the exception here
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("success", false);
            errorNode.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorNode);
        }

        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", true);
        successNode.put("message", "Password reset successfully");
        return ResponseEntity.ok().body(successNode);
    }
}