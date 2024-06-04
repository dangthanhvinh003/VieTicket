package com.example.VieTicketSystem.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.sql.Date;
import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private int orderId;
    private LocalDateTime date;
    private int total;
    private User user;
    private String vnpayData;
}
