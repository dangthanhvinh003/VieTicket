package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Ticket;

@Repository
public class TicketRepo {

    private static final String INSERT_STATEMENT = "INSERT INTO Ticket (qr_code, purchase_date, order_id, seat_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_STATEMENT = "UPDATE Ticket SET qr_code = ?, purchase_date = ?, order_id = ?, seat_id = ? WHERE ticket_id = ?";
    // private static final String READ_STATEMENT = "SELECT * FROM Ticket WHERE ticket_id = ?";
    // private static final String DELETE_STATEMENT = "DELETE FROM Ticket WHERE ticket_id = ?";
    // private static final String SELECT_ALL_STATEMENT = "SELECT * FROM Ticket";
    private static final String SELECT_BY_USER_ID_STATEMENT = "SELECT * FROM ticket_with_user WHERE user_id = ?";
    private static final String SELECT_BY_USER_ID_WITH_LIMIT = "SELECT * FROM ticket_with_user WHERE user_id = ? LIMIT ? OFFSET ?";
    private static final String COUNT_BY_USER_ID_STATEMENT = "SELECT COUNT(*) FROM ticket_with_user WHERE user_id = ?";

    public void saveNew(Ticket ticket) throws Exception {

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(INSERT_STATEMENT);
        ps.setString(1, ticket.getQrCode());
        ps.setDate(2, new Date(ticket.getPurchaseDate().getTime())); // Convert to SQL Date
        ps.setInt(3, ticket.getOrderId());
        ps.setInt(4, ticket.getSeatId());

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void updateExisting(Ticket ticket) throws Exception {

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(UPDATE_STATEMENT);
        ps.setString(1, ticket.getQrCode());
        ps.setDate(2, new Date(ticket.getPurchaseDate().getTime())); // Convert to SQL Date
        ps.setInt(3, ticket.getOrderId());
        ps.setInt(4, ticket.getSeatId());
        ps.setInt(5, ticket.getTicketId());

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public List<Ticket> findByUserId(int userId) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(SELECT_BY_USER_ID_STATEMENT);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        List<Ticket> tickets = new ArrayList<>();
        while (rs.next()) {
            Ticket ticket = new Ticket();
            ticket.setTicketId(rs.getInt("ticket_id"));
            ticket.setQrCode(rs.getString("qr_code"));
            ticket.setPurchaseDate(rs.getDate("purchase_date"));
            ticket.setOrderId(rs.getInt("order_id"));
            ticket.setSeatId(rs.getInt("seat_id"));
            tickets.add(ticket);
        }
        rs.close();
        ps.close();
        con.close();
        return tickets;
    }

    public List<Ticket> findByUserId(int userId, int limit, int offset) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(SELECT_BY_USER_ID_WITH_LIMIT);
        ps.setInt(1, userId);
        ps.setInt(2, limit);
        ps.setInt(3, offset);
        ResultSet rs = ps.executeQuery();
        List<Ticket> tickets = new ArrayList<>();
        while (rs.next()) {
            Ticket ticket = new Ticket();
            ticket.setTicketId(rs.getInt("ticket_id"));
            ticket.setQrCode(rs.getString("qr_code"));
            ticket.setPurchaseDate(rs.getDate("purchase_date"));
            ticket.setOrderId(rs.getInt("order_id"));
            ticket.setSeatId(rs.getInt("seat_id"));
            tickets.add(ticket);
        }
        rs.close();
        ps.close();
        con.close();
        return tickets;
    }

    public int countByUserId(int userId) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(COUNT_BY_USER_ID_STATEMENT);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        con.close();
        return count;
    }
}