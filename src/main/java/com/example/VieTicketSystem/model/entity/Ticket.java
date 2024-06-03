package com.example.VieTicketSystem.model.entity;
import java.sql.Date;

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
public class Ticket {
    private int ticketId;
    private String qrCode;
    private Date purchaseDate;
    private Order order;
    private Seat seat;
    private boolean isReturned;
    private boolean isCheckedIn;
}
