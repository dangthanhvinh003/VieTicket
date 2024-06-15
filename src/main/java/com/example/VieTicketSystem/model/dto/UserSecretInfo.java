package com.example.VieTicketSystem.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserSecretInfo {
    private int userId;
    private String secretKey;
    private LocalDateTime createdAt;
}
