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
    private int seatMapId;

    public Area(String name, int totalTicket, float ticketPrice, int eventId, int seatMapId) {
        this.name = name;
        this.totalTickets = totalTicket;
        this.ticketPrice = ticketPrice;
        this.event = new Event();
        this.event.setEventId(eventId);
        this.seatMapId = seatMapId;
    }
}
