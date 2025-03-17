package com.example.VieTicketSystem.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapDto {
    private int seatMapId;
    private int eventId;
    private String name;
    private String img;
    private String mapFile;
    private List<AreaWithRows> areas;
}
