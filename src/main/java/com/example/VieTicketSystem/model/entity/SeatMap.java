package com.example.VieTicketSystem.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SeatMap {
    private int seatMapId;
    private Event event;
    private String name;
    private String img;
    public SeatMap (String name, String img){
        this.name = name;
        this.img = img;
    }
}
