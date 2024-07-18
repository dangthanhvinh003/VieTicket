package com.example.VieTicketSystem.model.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String fullName;
    private String email;
    private String username;
    private String password;
    private String confirmPassword;
    private char role;

    // Getters and Setters
}
