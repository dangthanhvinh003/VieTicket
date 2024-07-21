package com.example.VieTicketSystem.model.dto;

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
public class EventStatistics {
    private double totalRevenue;
    private int ticketsSold;
    private int ticketsReturned;
    private int ticketsRemaining;
}
