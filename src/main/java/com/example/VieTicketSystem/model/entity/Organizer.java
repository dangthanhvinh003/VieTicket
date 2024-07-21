package com.example.VieTicketSystem.model.entity;

import lombok.*;

import java.sql.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Organizer extends User {
    private Date foundedDate;
    private String website;
    private String organizerAddr;
    private String organizerType;
    private boolean isActive;
}

