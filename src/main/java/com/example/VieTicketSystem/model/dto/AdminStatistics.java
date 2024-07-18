package com.example.VieTicketSystem.model.dto;

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
public class AdminStatistics {
    private double totalSoldAmountActiveEvents;
    private int totalOngoingEvents;
    private int totalApprovedEvents;
    private int totalRejectedEvents;
    private int totalRefundTicket;  // Thêm thuộc tính này
    private int totalPassEvent;
    private int totalInactiveOrganizers;
    private int totalActiveOrganizers;
    private int totalUsersExcludingOrganizers;
    private int totalBannedUsers;
}
