package com.example.VieTicketSystem.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Area {
    private int areaId;
    private String name;
    private int totalTickets;
    private float ticketPrice;
    private int eventId; 
}
