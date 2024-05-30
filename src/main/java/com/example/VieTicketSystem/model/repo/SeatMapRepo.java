package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;

@Repository
public class SeatMapRepo {

    public void addSeatMap(int eventId, String name, String img)
            throws ClassNotFoundException, SQLException {

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO SeatMap (event_id, name, img) VALUES (?, ?, ?)");
        ps.setInt(1, eventId);
        ps.setString(2, name);
        ps.setString(3, img);
        ps.executeUpdate();
        ps.close();
    }
}
