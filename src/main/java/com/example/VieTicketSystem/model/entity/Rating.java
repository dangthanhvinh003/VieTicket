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
public class Rating {
    private int ratingId;
    private int star;
    private int organizerId; // Đại diện cho quan hệ với bảng Event
    private int orderId;
}
