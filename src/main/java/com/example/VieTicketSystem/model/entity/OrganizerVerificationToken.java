package com.example.VieTicketSystem.model.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerVerificationToken {
    private int id;
    private String token;
    private Organizer organizer;
    private String familiarName;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
}
