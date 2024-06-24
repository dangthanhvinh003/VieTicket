package com.example.VieTicketSystem.model.entity;
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
public class Ticket {
    private int ticketId;
    private String qrCode;
    private LocalDateTime purchaseDate;
    private Order order;
    private Seat seat;
    private TicketStatus status;

    public enum TicketStatus {
        PURCHASED(0),
        FAILED(1),
        PENDING(2),
        RETURNED(3),
        CHECKED_IN(4),
        PENDING_REFUND(5);

        private final int value;

        TicketStatus(int value) {
            this.value = value;
        }

        public int toInteger() {
            return value;
        }

        public static TicketStatus fromInteger(int value) {
            for (TicketStatus status : TicketStatus.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return null;
        }
    }
}
