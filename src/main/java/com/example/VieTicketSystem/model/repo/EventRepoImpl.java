package com.example.VieTicketSystem.model.repo;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;

import com.example.VieTicketSystem.model.entity.Event;

public class EventRepoImpl implements EventRepo {

    private static final String URL = "jdbc:mysql://localhost:3307/VieTicket1";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "thanhvinh";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL driver", e);
        }
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
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
                // Set the organizer if applicable
                events.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return events;
    }
}
