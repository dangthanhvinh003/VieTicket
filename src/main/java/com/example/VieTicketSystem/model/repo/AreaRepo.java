package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.springframework.stereotype.Repository;

@Repository
public class AreaRepo {
    public void addArea(String areaName, int totalTicket, int eventId,String ticketPrice,int idSeatMap)
            throws ClassNotFoundException, SQLException, ParseException {
                NumberFormat format = NumberFormat.getInstance(Locale.US);
        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO AREA (event_id, name, total_tickets,ticket_price,seat_map_id) VALUES (?, ?, ?,?,?)");
        ps.setInt(1, eventId);
        ps.setString(2, areaName);
        ps.setInt(3, totalTicket);
        ps.setFloat(4, format.parse(ticketPrice).floatValue());
        ps.setInt(5, idSeatMap);
        ps.executeUpdate();
        ps.close();
    }

    public int getIdAreaEventId(int eventId) throws ClassNotFoundException, SQLException {
        int areaId = -1; // Giá trị mặc định khi không tìm thấy khu vực

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = connection.prepareStatement(
                "SELECT area_id FROM AREA WHERE event_id = ? ");
        ps.setInt(1, eventId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            areaId = rs.getInt("area_id");
        }

        rs.close();
        ps.close();
        connection.close();

        return areaId;
    }
    public int getIdAreaEventIdAndName(int eventId, String name) throws ClassNotFoundException, SQLException {
        int areaId = -1; // Giá trị mặc định khi không tìm thấy khu vực

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = connection.prepareStatement(
                "SELECT area_id FROM AREA WHERE event_id = ? and name = ? ");
        ps.setInt(1, eventId);
        ps.setString(2, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            areaId = rs.getInt("area_id");
        }

        rs.close();
        ps.close();
        connection.close();

        return areaId;
    }

}
