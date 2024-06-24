package com.example.VieTicketSystem.model.entity;
import java.sql.Date;
import java.time.LocalDateTime;

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
    private LocalDateTime createdOn;
    private Order order;
    private RefundStatus status;

    public enum RefundStatus {
        SUCCESS(0),
        FAILED(1),
        PENDING(2);

        private final int value;

        RefundStatus(int value) {
            this.value = value;
        }

        public int toInteger() {
            return value;
        }

        public static RefundStatus fromInteger(int value) {
            for (RefundStatus status : RefundStatus.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return null;
        }
    }
}
