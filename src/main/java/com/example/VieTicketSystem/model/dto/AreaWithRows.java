package com.example.VieTicketSystem.model.dto;

import com.example.VieTicketSystem.model.entity.Area;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AreaWithRows {
    private Area area;
    private List<RowWithSeats> rows;
}
