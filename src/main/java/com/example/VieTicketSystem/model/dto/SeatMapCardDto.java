package com.example.VieTicketSystem.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapCardDto {
    private int seatMapId;
    private String eventName;
    private int eventId;
    private String img;
    private String location;
}
