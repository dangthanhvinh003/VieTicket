package com.example.VieTicketSystem.model.service;

import com.example.VieTicketSystem.model.dto.AreaWithRows;
import com.example.VieTicketSystem.model.dto.EventWithAreas;
import com.example.VieTicketSystem.model.dto.RowWithSeats;
import com.example.VieTicketSystem.model.entity.Area;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Row;
import com.example.VieTicketSystem.model.entity.Seat;
import com.example.VieTicketSystem.model.repo.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseTicketService {
    private final AreaRepo areaRepo;
    private final EventRepo eventRepo;
    private final RowRepo rowRepo;
    private final SeatRepo seatRepo;

    public PurchaseTicketService(AreaRepo areaRepo, EventRepo eventRepo, RowRepo rowRepo, SeatRepo seatRepo) {
        this.areaRepo = areaRepo;
        this.eventRepo = eventRepo;
        this.rowRepo = rowRepo;
        this.seatRepo = seatRepo;
    }

    public EventWithAreas getEventWithAreas(int eventId) throws Exception {
        Event event = eventRepo.findById(eventId);
        List<AreaWithRows> areasWithRows = new ArrayList<>();
        for (Area area : areaRepo.findByEventId(eventId)) {
            List<RowWithSeats> rowsWithSeats = new ArrayList<>();
            for (Row row : rowRepo.findByAreaId(area.getAreaId())) {
                List<Seat> seats = seatRepo.findByAreaId(row.getRowId());
                rowsWithSeats.add(new RowWithSeats(row, seats));
            }
            areasWithRows.add(new AreaWithRows(area, rowsWithSeats));
        }
        return new EventWithAreas(event, areasWithRows);
    }
}
