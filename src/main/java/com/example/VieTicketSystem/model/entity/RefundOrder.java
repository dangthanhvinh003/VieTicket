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
public class RefundOrder {
    private int refundOrderId;
    private Date date;
    private User user; // Đại diện cho quan hệ với bảng User
    private Order order;
}
