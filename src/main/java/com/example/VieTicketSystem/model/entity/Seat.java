package com.example.VieTicketSystem.model.entity;

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
public class Seat {
    private int seatId;
    private String number;
    private float ticketPrice;
    private boolean isBuy;
    private boolean isCheckedIn;
    private int rowId;
}
