package com.example.VieTicketSystem.model.entity;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Area {
    private int areaId;
    private String name;
    private int totalTickets;
    private float ticketPrice;
    private Event event;
}
