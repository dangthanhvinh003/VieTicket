package com.example.VieTicketSystem.model.dto;

import com.example.VieTicketSystem.model.entity.Row;
import com.example.VieTicketSystem.model.entity.Seat;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RowWithSeats {
    private Row row;
    private List<Seat> seats;
}
