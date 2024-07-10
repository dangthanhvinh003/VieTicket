package com.example.VieTicketSystem.model.dto;

import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Order;
import lombok.*;

@Data
public class OrderWithEvent {
    private Order order;
    private Event event;
}
