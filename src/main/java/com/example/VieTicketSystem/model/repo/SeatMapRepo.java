package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
