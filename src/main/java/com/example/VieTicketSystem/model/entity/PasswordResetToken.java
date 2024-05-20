package com.example.VieTicketSystem.model.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {
    
    private int id;
    private int userId;
    private String token;
    private LocalDateTime expiryDate;
}
