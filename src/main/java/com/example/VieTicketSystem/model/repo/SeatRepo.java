package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;

@Repository
public class SeatRepo {
    public void addSeat(String seatNumber, String ticketPrice, int rowId)
            throws ClassNotFoundException, SQLException {

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO SEAT (number, ticket_price,is_buy,is_checkedin,row_id) VALUES (?, ?,?,?,?)");

        ps.setString(1, seatNumber);
        ps.setFloat(2, Float.parseFloat(ticketPrice));
        ps.setBoolean(3, false);
        ps.setBoolean(4, false);
        ps.setInt(5, rowId);
        ps.executeUpdate();
        ps.close();
    }
}
