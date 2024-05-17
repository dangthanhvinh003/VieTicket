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
    private int orderId; // Đại diện cho quan hệ với bảng Order
    private int seatId; 
}
