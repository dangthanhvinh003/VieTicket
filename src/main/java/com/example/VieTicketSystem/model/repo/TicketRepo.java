package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Ticket;

@Repository
public class TicketRepo {

    private static final String INSERT_STATEMENT = "INSERT INTO Ticket (qr_code, purchase_date, order_id, seat_id, is_returned, is_checked_in) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_STATEMENT = "UPDATE Ticket SET qr_code = ?, purchase_date = ?, order_id = ?, seat_id = ?, is_returned = ?, is_checked_in = ? WHERE ticket_id = ?";
    private static final String SELECT_BY_USER_ID_STATEMENT = "SELECT * FROM ticket_with_user WHERE user_id = ?";
    private static final String SELECT_BY_USER_ID_WITH_LIMIT = "SELECT * FROM ticket_with_user WHERE user_id = ? LIMIT ? OFFSET ?";
    private static final String COUNT_BY_USER_ID_STATEMENT = "SELECT COUNT(*) FROM ticket_with_user WHERE user_id = ?";
    private static final String SELECT_BY_QR_CODE_STATEMENT = "SELECT * FROM Ticket WHERE qr_code LIKE ?";
    private final OrderRepo orderRepo;
    private final SeatRepo seatRepo;

    public TicketRepo(OrderRepo orderRepo, SeatRepo seatRepo) {
        this.orderRepo = orderRepo;
        this.seatRepo = seatRepo;
    }

    public void saveNew(Ticket ticket) throws Exception {

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(INSERT_STATEMENT);
        ps.setString(1, ticket.getQrCode());
        // Chuyển đổi LocalDateTime sang Timestamp
        if (ticket.getPurchaseDate() != null) {
            ps.setTimestamp(2, Timestamp.valueOf(ticket.getPurchaseDate()));
        } else {
            ps.setTimestamp(2, null); // Xử lý trường hợp purchaseDate là null nếu cần thiết
        }
        ps.setInt(3, ticket.getOrder().getOrderId());
        ps.setInt(4, ticket.getSeat().getSeatId());
        ps.setBoolean(5, ticket.isReturned());
        ps.setBoolean(6, ticket.isCheckedIn());

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
        if (ticket.getPurchaseDate() != null) {
            ps.setTimestamp(2, Timestamp.valueOf(ticket.getPurchaseDate()));
        } else {
            ps.setTimestamp(2, null); // Xử lý trường hợp purchaseDate là null nếu cần thiết
        }
        ps.setInt(3, ticket.getOrder().getOrderId());
        ps.setInt(4, ticket.getSeat().getSeatId());
        ps.setBoolean(5, ticket.isReturned());
        ps.setBoolean(6, ticket.isCheckedIn());
        ps.setInt(7, ticket.getTicketId());

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
            // Lấy giá trị từ ResultSet và chuyển đổi thành LocalDateTime
            Timestamp timestamp = rs.getTimestamp("purchase_date");
            if (timestamp != null) {
                ticket.setPurchaseDate(timestamp.toLocalDateTime());
            } else {
                ticket.setPurchaseDate(null); // Xử lý trường hợp purchase_date là null nếu cần thiết
            }
            ticket.setOrder(orderRepo.findById(rs.getInt("order_id")));
            ticket.setSeat(seatRepo.findById(rs.getInt("seat_id")));
            ticket.setReturned(rs.getBoolean("is_returned"));
            ticket.setCheckedIn(rs.getBoolean("is_checked_in"));
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
            // Lấy giá trị từ ResultSet và chuyển đổi thành LocalDateTime
            Timestamp timestamp = rs.getTimestamp("purchase_date");
            if (timestamp != null) {
                ticket.setPurchaseDate(timestamp.toLocalDateTime());
            } else {
                ticket.setPurchaseDate(null); // Xử lý trường hợp purchase_date là null nếu cần thiết
            }
            ticket.setOrder(orderRepo.findById(rs.getInt("order_id")));
            ticket.setSeat(seatRepo.findById(rs.getInt("seat_id")));
            ticket.setReturned(rs.getBoolean("is_returned"));
            ticket.setCheckedIn(rs.getBoolean("is_checked_in"));
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

    public Ticket findByQrCode(String qrCode) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(SELECT_BY_QR_CODE_STATEMENT);
        ps.setString(1, qrCode);
        ResultSet rs = ps.executeQuery();
        Ticket ticket = null;
        if (rs.next()) {
            ticket = new Ticket();
            ticket.setTicketId(rs.getInt("ticket_id"));
            ticket.setQrCode(rs.getString("qr_code"));
            // Lấy giá trị từ ResultSet và chuyển đổi thành LocalDateTime
            Timestamp timestamp = rs.getTimestamp("purchase_date");
            if (timestamp != null) {
                ticket.setPurchaseDate(timestamp.toLocalDateTime());
            } else {
                ticket.setPurchaseDate(null); // Xử lý trường hợp purchase_date là null nếu cần thiết
            }
            ticket.setOrder(orderRepo.findById(rs.getInt("order_id")));
            ticket.setSeat(seatRepo.findById(rs.getInt("seat_id")));
            ticket.setReturned(rs.getBoolean("is_returned"));
            ticket.setCheckedIn(rs.getBoolean("is_checked_in"));
        }
        rs.close();
        ps.close();
        con.close();
        return ticket;
    }
}