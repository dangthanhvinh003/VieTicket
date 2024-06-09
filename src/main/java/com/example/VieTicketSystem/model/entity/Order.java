package com.example.VieTicketSystem.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private int orderId;
    private LocalDateTime date;
    private long total;
    private User user;
    private String vnpayData;
    private PaymentStatus status;

    public enum PaymentStatus {
        SUCCESS(0),
        FAILED(1),
        PENDING(2),
        REFUNDED(3),
        PENDING_REFUND(4);

        private final int value;

        PaymentStatus(int value) {
            this.value = value;
        }

        public int toInteger() {
            return value;
        }

        public static PaymentStatus fromInteger(int value) {
            for (PaymentStatus status : PaymentStatus.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return null;
        }
    }
}
