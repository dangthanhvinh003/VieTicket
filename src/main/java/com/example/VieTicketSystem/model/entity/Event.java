package com.example.VieTicketSystem.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.sql.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private int eventId;
    private String name;
    private String description;
    private Date startDate;
    private String location;
    private String type;
    private Date ticketSaleDate;
    private Date endDate;
    private Organizer organizer;
    private String seatMap;
    private String poster;
    private String banner;
    private boolean isApprove;
}
