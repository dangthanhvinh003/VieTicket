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
    private PaymentStatus status;

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUND;

        public static PaymentStatus fromInteger(int x) {
            return switch (x) {
                case 0 -> SUCCESS;
                case 1 -> FAILED;
                case 2 -> PENDING;
                case 3 -> REFUND;
                default -> null;
            };
        }

        public int toInteger() {
            return switch (this) {
                case SUCCESS -> 0;
                case FAILED -> 1;
                case PENDING -> 2;
                case REFUND -> 3;
            };
        }
    }
}
