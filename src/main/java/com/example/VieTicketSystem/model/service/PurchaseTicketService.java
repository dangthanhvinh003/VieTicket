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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseTicketService {
    private final AreaRepo areaRepo;
    private final EventRepo eventRepo;
    private final RowRepo rowRepo;
    private final SeatRepo seatRepo;
    private final OrganizerRepo organizerRepo;

    public PurchaseTicketService(AreaRepo areaRepo, EventRepo eventRepo, RowRepo rowRepo, SeatRepo seatRepo, OrganizerRepo organizerRepo) {
        this.areaRepo = areaRepo;
        this.eventRepo = eventRepo;
        this.rowRepo = rowRepo;
        this.seatRepo = seatRepo;
        this.organizerRepo = organizerRepo;
    }

    public EventWithAreas getEventWithAreas(int eventId) throws Exception {
        String query = "SELECT e.*, a.*, r.*, s.* " +
                "FROM Event e " +
                "JOIN Area a ON a.event_id = e.event_id " +
                "JOIN `Row` r ON r.area_id = a.area_id " +
                "JOIN Seat s ON s.row_id = r.row_id " +
                "WHERE e.event_id = ? " +
                "ORDER BY a.area_id, r.row_id, s.seat_id";

        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                EventWithAreas eventWithAreas = null;
                AreaWithRows currentAreaWithRows = null;
                RowWithSeats currentRowWithSeats = null;

                while (resultSet.next()) {
                    if (eventWithAreas == null) {
                        Event event = extractEventFromResultSet(resultSet);
                        eventWithAreas = new EventWithAreas(event, new ArrayList<>());
                    }

                    int areaId = resultSet.getInt("a.area_id");
                    if (currentAreaWithRows == null || currentAreaWithRows.getArea().getAreaId() != areaId) {
                        Area area = extractAreaFromResultSet(resultSet);
                        currentAreaWithRows = new AreaWithRows(area, new ArrayList<>());
                        eventWithAreas.getAreas().add(currentAreaWithRows);
                    }

                    int rowId = resultSet.getInt("r.row_id");
                    if (currentRowWithSeats == null || currentRowWithSeats.getRow().getRowId() != rowId) {
                        Row row = extractRowFromResultSet(resultSet);
                        currentRowWithSeats = new RowWithSeats(row, new ArrayList<>());
                        currentAreaWithRows.getRows().add(currentRowWithSeats);
                    }

                    Seat seat = extractSeatFromResultSet(resultSet);
                    currentRowWithSeats.getSeats().add(seat);
                }

                return eventWithAreas;
            }
        }
    }

    private Event extractEventFromResultSet(ResultSet resultSet) throws Exception {
        Event event = new Event();
        event.setEventId(resultSet.getInt("e.event_id"));
        event.setName(resultSet.getString("e.name"));
        event.setDescription(resultSet.getString("e.description"));
        event.setStartDate(resultSet.getTimestamp("e.start_date").toLocalDateTime());
        event.setLocation(resultSet.getString("e.location"));
        event.setType(resultSet.getString("e.type"));
        event.setTicketSaleDate(resultSet.getTimestamp("e.ticket_sale_date").toLocalDateTime());
        event.setEndDate(resultSet.getTimestamp("e.end_date").toLocalDateTime());
        event.setOrganizer(organizerRepo.findById(resultSet.getInt("e.organizer_id")));
        event.setPoster(resultSet.getString("e.poster"));
        event.setBanner(resultSet.getString("e.banner"));
        event.setApproved(resultSet.getInt("e.is_approve"));

        Timestamp startTimestamp = resultSet.getTimestamp("e.start_date");
        if (startTimestamp != null) {
            event.setStartDate(startTimestamp.toLocalDateTime());
        }

        Timestamp ticketSaleTimestamp = resultSet.getTimestamp("e.ticket_sale_date");
        if (ticketSaleTimestamp != null) {
            event.setTicketSaleDate(ticketSaleTimestamp.toLocalDateTime());
        }

        Timestamp endTimestamp = resultSet.getTimestamp("e.end_date");
        if (endTimestamp != null) {
            event.setEndDate(endTimestamp.toLocalDateTime());
        }

        return event;
    }

    private Area extractAreaFromResultSet(ResultSet resultSet) throws SQLException {
        Area area = new Area();
        area.setAreaId(resultSet.getInt("a.area_id"));
        area.setName(resultSet.getString("a.name"));
        area.setTicketPrice(resultSet.getFloat("a.ticket_price"));
        return area;
    }

    private Row extractRowFromResultSet(ResultSet resultSet) throws SQLException {
        Row row = new Row();
        row.setRowId(resultSet.getInt("r.row_id"));
        row.setRowName(resultSet.getString("r.row_name"));
        return row;
    }

    private Seat extractSeatFromResultSet(ResultSet resultSet) throws SQLException {
        Seat seat = new Seat();
        seat.setSeatId(resultSet.getInt("s.seat_id"));
        seat.setNumber(resultSet.getString("s.number"));
        seat.setTaken(resultSet.getBoolean("s.is_taken"));
        seat.setTicketPrice(resultSet.getFloat("s.ticket_price"));
        return seat;
    }

    public boolean isTicketSaleDateStarted(int eventId) throws Exception {
        return eventRepo.findById(eventId).getTicketSaleDate().isAfter(java.time.LocalDateTime.now());
    }

    public boolean isEventPassed(int eventId) throws Exception {
        return eventRepo.findById(eventId).getEndDate().isBefore(java.time.LocalDateTime.now());
    }

    public boolean isEventExist(int eventId) throws Exception {
        return eventRepo.findById(eventId) != null;
    }

    public boolean areSeatsTaken(List<Integer> seats) throws Exception {
        for (int seatId : seats) {
            if (seatRepo.findById(seatId).isTaken()) {
                return true;
            }
        }
        return false;
    }

    public List<Seat> getSeatsBySeatIds(List<Integer> seatIds) throws Exception {
        List<Seat> seats = new ArrayList<>();
        for (int seatId : seatIds) {
            seats.add(seatRepo.findById(seatId));
        }
        return seats;
    }
}
