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
public class Seat {
    private int seatId;
    private String number;
    private float ticketPrice;
    private TakenStatus taken;
    private Row row;

    public boolean isAvailable() {
        return taken == TakenStatus.AVAILABLE;
    }

    public enum TakenStatus {
        AVAILABLE(0),
        TAKEN(1),
        RESERVED(2);

        private final int value;

        private TakenStatus(int value) {
            this.value = value;
        }

        public int toInteger() {
            return value;
        }

        public static TakenStatus fromInteger(int value) {
            return switch (value) {
                case 0 -> AVAILABLE;
                case 1 -> TAKEN;
                case 2 -> RESERVED;
                default -> null;
            };
        }
    }
}
