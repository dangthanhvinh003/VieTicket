package com.example.VieTicketSystem.model.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Represents a refund order within the ticketing system.
 * It tracks the lifecycle of a refund request from creation to completion.
 */
@Data
public class RefundOrder {
    private LocalDateTime createdOn; // The date and time when the refund order was created.
    private Order order; // The associated order for which the refund is requested.
    private int total; // The total amount to be refunded (does not include the 30% cut).
    private RefundStatus status; // The current status of the refund order.
    private LocalDateTime approvedOn; // The date and time when the refund order was approved.
    private LocalDateTime refundedOn; // The date and time when the refund was processed.
    private int actualRefund; // The actual amount refunded to the user (70% of the total).

    public int getActualRefundAmount() {
        return (int) (total * 0.7);
    }

    /**
     * Enumerates the possible states of a refund order through its lifecycle.
     * - CREATED: Initial state when a refund request is made by the user.
     * - APPROVED/REJECTED: State set by the organizer after reviewing the refund request.
     * - PENDING: State indicating the refund request has been sent to the payment service.
     * - SUCCESS/FAILED: Final states indicating the outcome of the refund process.
     */
    public enum RefundStatus {
        SUCCESS(0), // Indicates the refund was successfully processed.
        FAILED(1), // Indicates the refund process failed.
        PENDING(2), // Indicates the refund is pending processing by the payment service.
        APPROVED(3), // Indicates the refund request has been approved by the organizer.
        REJECTED(4), // Indicates the refund request has been rejected by the organizer.
        CREATED(5); // Initial state when the refund request is created by the user.

        private final int value; // Numeric representation of the refund status.

        RefundStatus(int value) {
            this.value = value;
        }

        /**
         * Retrieves the RefundStatus enum corresponding to the given numeric value.
         *
         * @param value The numeric value to convert to a RefundStatus enum.
         * @return The RefundStatus enum, or null if the value does not correspond to any RefundStatus.
         */
        public static RefundStatus fromInteger(int value) {
            for (RefundStatus status : RefundStatus.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            return null;
        }

        /**
         * Converts the RefundStatus enum to an integer
         *
         * @return The corresponding integer.
         */
        public int toInteger() {
            return value;
        }
    }
}