package com.example.VieTicketSystem.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.VieTicketSystem.model.dto.SeatMapCardDto;
import com.example.VieTicketSystem.model.dto.SeatMapDto;
import com.example.VieTicketSystem.model.entity.SeatMap;
import org.springframework.stereotype.Repository;

@Repository
public class SeatMapRepo {

    private final EventRepo eventRepo;

    public SeatMapRepo(EventRepo eventRepo) {
        this.eventRepo = eventRepo;
    }

    public SeatMap getSeatMapByEventId(int eventId) throws SQLException {
        Connection connection = ConnectionPoolManager.getConnection();

        String query = "SELECT * FROM SeatMap WHERE event_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, eventId);

        ResultSet rs = ps.executeQuery();
        SeatMap seatMap = null;
        if (rs.next()) {
            seatMap = new SeatMap();
            seatMap.setSeatMapId(rs.getInt("seat_map_id"));
            seatMap.setEvent(eventRepo.getEventById(rs.getInt("event_id")));
            seatMap.setName(rs.getString("name"));
            seatMap.setImg(rs.getString("img"));
            seatMap.setMapFile(rs.getString("map_file")); // Set the new mapFile field
        }

        rs.close();
        ps.close();
        connection.close();

        return seatMap;
    }

    public void addSeatMap(int eventId, String name, String img) throws SQLException {

        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO SeatMap (event_id, name, img) VALUES (?, ?, ?)");
        ps.setInt(1, eventId);
        ps.setString(2, name);
        ps.setString(3, img);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public void addSeatMapWithEditor(int eventId, String name, String img, String json) throws SQLException {

        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO SeatMap (event_id, name, img, map_file) VALUES (?, ?, ?, ?)");
        ps.setInt(1, eventId);
        ps.setString(2, name);
        ps.setString(3, img);
        ps.setString(4, json);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public int getSeatMapIdByEventRepo(int eventId) throws SQLException {

        Connection connection = ConnectionPoolManager.getConnection();

        String query = "SELECT seat_map_id FROM SeatMap WHERE event_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, eventId);

        ResultSet rs = ps.executeQuery();
        int seatMapId = -1;
        if (rs.next()) {
            seatMapId = rs.getInt("seat_map_id");
        }

        rs.close();
        ps.close();
        connection.close();

        return seatMapId;
    }

    public List<SeatMapCardDto> getSeatMapList(String search) throws SQLException {
        Connection connection = ConnectionPoolManager.getConnection();

        String query = "SELECT sm.seat_map_id, e.event_id, sm.img, e.location " +
                "FROM seatmap sm " +
                "INNER JOIN event e ON sm.event_id = e.event_id " +
                "WHERE e.location LIKE ? " +
                "LIMIT 10";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, "%" + search + "%");

        ResultSet rs = ps.executeQuery();
        List<SeatMapCardDto> seatMapList = new ArrayList<>();
        while (rs.next()) {
            SeatMapCardDto seatMap = new SeatMapCardDto();
            seatMap.setSeatMapId(rs.getInt("seat_map_id"));
            seatMap.setEventId(rs.getInt("event_id"));
            seatMap.setImg(rs.getString("img"));
            seatMap.setLocation(rs.getString("location"));
            seatMapList.add(seatMap);
        }

        rs.close();
        ps.close();
        connection.close();

        return seatMapList;
    }

    public List<SeatMapCardDto> getSeatMapListByUserId(int userId) throws SQLException {

        Connection connection = ConnectionPoolManager.getConnection();

        String query = "SELECT sm.seat_map_id, e.event_id, e.name, sm.img, e.location FROM seatmap sm inner join event e on sm.event_id = e.event_id where e.organizer_id = ?;";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();
        List<SeatMapCardDto> seatMapList = new ArrayList<>();
        while (rs.next()) {
            SeatMapCardDto seatMap = new SeatMapCardDto();
            seatMap.setSeatMapId(rs.getInt("seat_map_id"));
            seatMap.setEventId(rs.getInt("event_id"));
            seatMap.setEventName(rs.getString("name"));
            seatMap.setImg(rs.getString("img"));
            seatMap.setLocation(rs.getString("location"));
            seatMapList.add(seatMap);
        }

        rs.close();
        ps.close();
        connection.close();

        return seatMapList;
    }

    public void deleteSeatMapByEventId(int eventId) throws SQLException {

        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM SeatMap WHERE event_id = ?");
        ps.setInt(1, eventId);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }
}
