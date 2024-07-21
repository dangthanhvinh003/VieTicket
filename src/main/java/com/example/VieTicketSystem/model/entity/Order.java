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

    public boolean isPendingRefund() {
        return status == PaymentStatus.PENDING_PARTIAL_REFUND || status == PaymentStatus.PENDING_TOTAL_REFUND;
    }

    public boolean isRefunded() {
        return status == PaymentStatus.TOTALLY_REFUNDED || status == PaymentStatus.PARTIALLY_REFUNDED;
    }

    public enum PaymentStatus {
        SUCCESS(0),
        FAILED(1),
        PENDING(2),
        TOTALLY_REFUNDED(3),
        PARTIALLY_REFUNDED(4),
        PENDING_TOTAL_REFUND(5),
        PENDING_PARTIAL_REFUND(6);

        private final int value;

        PaymentStatus(int value) {
            this.value = value;
        }

        public static PaymentStatus fromInteger(int value) {
            for (PaymentStatus status : PaymentStatus.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return null;
        }

        public int toInteger() {
            return value;
        }
    }
}
