package com.example.VieTicketSystem.config;

import com.example.VieTicketSystem.model.service.OrderService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final OrderService orderService;

    public SchedulerConfig(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 300_000)
    public void invalidatePendingOrders() throws Exception {
        orderService.processPendingOrders();
    }
}
