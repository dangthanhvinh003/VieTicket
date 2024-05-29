package com.example.VieTicketSystem.model.repo;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;

import com.example.VieTicketSystem.model.entity.Event;

@Repository
public class EventRepo {

    static {
        try {
            Class.forName(Baseconnection.nameClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL driver", e);
        }
    }
    @Autowired
    OrganizerRepo organizerRepo = new OrganizerRepo();

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Event");
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Event event = new Event();
                event.setEventId(resultSet.getInt("event_id"));
                event.setName(resultSet.getString("name"));
                event.setDescription(resultSet.getString("description"));
                event.setStartDate(resultSet.getDate("start_date"));
                event.setLocation(resultSet.getString("location"));
                event.setType(resultSet.getString("type"));
                event.setTicketSaleDate(resultSet.getDate("ticket_sale_date"));
                event.setEndDate(resultSet.getDate("end_date"));
                event.setPoster(resultSet.getString("poster"));
                event.setBanner(resultSet.getString("banner"));
                event.setApprove(resultSet.getBoolean("is_approve"));
                // Set the organizer if applicable
                events.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return events;
    }

    public Event getEventByName(String eventName) {
        Event event = null;
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Event WHERE name = ?");) {

            statement.setString(1, eventName); // Truyền tên sự kiện cụ thể vào câu lệnh SQL

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    event = new Event();
                    event.setEventId(resultSet.getInt("event_id"));
                    event.setName(resultSet.getString("name"));
                    event.setDescription(resultSet.getString("description"));
                    event.setStartDate(resultSet.getDate("start_date"));
                    event.setLocation(resultSet.getString("location"));
                    event.setType(resultSet.getString("type"));
                    event.setTicketSaleDate(resultSet.getDate("ticket_sale_date"));
                    event.setEndDate(resultSet.getDate("end_date"));
                    event.setPoster(resultSet.getString("poster"));
                    event.setBanner(resultSet.getString("banner"));
                    event.setApprove(resultSet.getBoolean("is_approve"));
                    // Set the organizer if applicable
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public int addEvent(String name, String description, Date startDate, String location, String type,
                    Date ticketSaleDate, Date endDate, int organizerId, String poster, String banner)
                    throws ClassNotFoundException, SQLException {

    Class.forName(Baseconnection.nameClass);
    Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
    
    // Sử dụng tùy chọn RETURN_GENERATED_KEYS để lấy khóa tự động tăng
    PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO Event (name, description, start_date, location, type, ticket_sale_date, end_date, organizer_id, poster, banner, is_approve) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS);
    
    ps.setString(1, name);
    ps.setString(2, description);
    ps.setDate(3, startDate);
    ps.setString(4, location);
    ps.setString(5, type);
    ps.setDate(6, ticketSaleDate);
    ps.setDate(7, endDate);
    ps.setInt(8, organizerId);
    ps.setString(9, poster);
    ps.setString(10, banner);
    ps.setBoolean(11, false);
    ps.executeUpdate();
    
    // Lấy khóa tự động tăng vừa được tạo
    ResultSet rs = ps.getGeneratedKeys();
    int eventId = 0;
    if (rs.next()) {
        eventId = rs.getInt(1);
    }
    
    rs.close();
    ps.close();
    connection.close();
    
    return eventId;
}

}
