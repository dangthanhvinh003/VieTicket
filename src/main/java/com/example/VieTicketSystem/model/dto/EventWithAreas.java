package com.example.VieTicketSystem.model.dto;

import com.example.VieTicketSystem.model.entity.Event;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EventWithAreas {
    private Event event;
    private List<AreaWithRows> areas;
}
