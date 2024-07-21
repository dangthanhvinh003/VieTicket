package com.example.VieTicketSystem.repo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.VieTicketSystem.model.entity.Order;
import com.example.VieTicketSystem.model.entity.Organizer;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Ticket;

@Repository
public class TicketRepo {

    private static final String INSERT_STATEMENT = "INSERT INTO Ticket (qr_code, purchase_date, order_id, seat_id, status) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_STATEMENT = "UPDATE Ticket SET qr_code = ?, purchase_date = ?, order_id = ?, seat_id = ?, status = ? WHERE ticket_id = ?";
    private static final String SELECT_BY_USER_ID_STATEMENT = "SELECT * FROM Ticket JOIN `Order` O ON O.order_id = Ticket.order_id WHERE user_id = ?";
    private static final String SELECT_BY_USER_ID_WITH_LIMIT = "SELECT * FROM Ticket JOIN `Order` O ON O.order_id = Ticket.order_id WHERE user_id = ? ORDER BY ticket_id DESC LIMIT ? OFFSET ?";
    private static final String COUNT_BY_USER_ID_STATEMENT = "SELECT COUNT(*) FROM Ticket JOIN `Order` O ON O.order_id = Ticket.order_id WHERE user_id = ?";
    private static final String SELECT_BY_QR_CODE_STATEMENT = "SELECT * FROM Ticket WHERE qr_code = ?";
    private static final String SELECT_BY_ORDER_ID_STATEMENT = "SELECT * FROM Ticket WHERE order_id = ?";
    private static final String UPDATE_SUCCESS_IN_BULK_SQL = "UPDATE Ticket SET purchase_date = ?, status = ? WHERE ticket_id = ?";
    private static final String UPDATE_FAILURE_IN_BULK_SQL = "UPDATE Ticket SET status = ? WHERE ticket_id = ?";
    private static final String UPDATE_STATUS_BY_ORDER_ID_SQL = "UPDATE Ticket SET status = ? WHERE order_id = ?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM Ticket WHERE ticket_id = ?";

    private final OrderRepo orderRepo;
    private final SeatRepo seatRepo;

    public TicketRepo(OrderRepo orderRepo, SeatRepo seatRepo, ConnectionPoolManager connectionPoolManager) {
        this.orderRepo = orderRepo;
        this.seatRepo = seatRepo;
    }

    public Ticket findById(int ticketId) throws Exception {
        Ticket ticket = null;
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(FIND_BY_ID_SQL);
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
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
                ticket.setStatus(Ticket.TicketStatus.fromInteger(rs.getInt("status")));
            }
            rs.close();
            ps.close();
        }
        return ticket;
    }

    public void updateStatusByOrderId(int orderId, int status) throws Exception {

        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(UPDATE_STATUS_BY_ORDER_ID_SQL);
            ps.setInt(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
            ps.close();
        }
    }

    public void setSuccessInBulk(List<Integer> ticketIds, LocalDateTime purchaseDate, int successStatus) throws Exception {
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(UPDATE_SUCCESS_IN_BULK_SQL);
            for (Integer ticketId : ticketIds) {
                ps.setTimestamp(1, purchaseDate == null ? null : Timestamp.valueOf(purchaseDate));
                ps.setInt(2, successStatus);
                ps.setInt(3, ticketId);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        }
    }

    public void setStatusInBulk(List<Integer> ticketIds, int status) throws Exception {
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(UPDATE_FAILURE_IN_BULK_SQL);
            for (Integer ticketId : ticketIds) {
                ps.setInt(1, status);
                ps.setInt(2, ticketId);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        }
    }

    public void updateStatusByOrderIdAndStatus(int orderId, int status, int newStatus) throws Exception {
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE Ticket SET status = ? WHERE order_id = ? AND status = ?");
            ps.setInt(1, newStatus);
            ps.setInt(2, orderId);
            ps.setInt(3, status);
            ps.executeUpdate();
            ps.close();
        }
    }

    public List<Integer> findSeatIdsByOrderIdAndStatus(int orderid, int status) throws SQLException {
        List<Integer> seatIds = new ArrayList<>();

        try (Connection con = ConnectionPoolManager.getConnection()) {

            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT seat_id FROM Ticket WHERE order_id = ? AND status = ?");
            ps.setInt(1, orderid);
            ps.setInt(2, status);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                seatIds.add(rs.getInt("seat_id"));
            }
            rs.close();
            ps.close();
        }

        return seatIds;
    }

    public List<Ticket> findByOrderId(int orderId) throws Exception {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(SELECT_BY_ORDER_ID_STATEMENT);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
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
                ticket.setOrder(new Order() {{
                    setOrderId(rs.getInt("order_id"));
                }});
                ticket.setSeat(seatRepo.findById(rs.getInt("seat_id")));
                ticket.setStatus(Ticket.TicketStatus.fromInteger(rs.getInt("status")));
                tickets.add(ticket);
            }
            rs.close();
            ps.close();
        }
        return tickets;
    }

    public void saveNew(String qrCode, LocalDateTime purchaseDate, int orderId, int seatId, Ticket.TicketStatus status) throws Exception {
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(INSERT_STATEMENT);
            ps.setString(1, qrCode);
            // Chuyển đổi LocalDateTime sang Timestamp
            if (purchaseDate != null) {
                ps.setTimestamp(2, Timestamp.valueOf(purchaseDate));
            } else {
                ps.setTimestamp(2, null); // Xử lý trường hợp purchaseDate là null nếu cần thiết
            }
            ps.setInt(3, orderId);
            ps.setInt(4, seatId);
            ps.setInt(5, status.toInteger());

            ps.executeUpdate();
            ps.close();
        }
    }

    public void saveNew(Ticket ticket) throws Exception {
        try (Connection con = ConnectionPoolManager.getConnection()) {
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
            ps.setInt(5, ticket.getStatus().toInteger());

            ps.executeUpdate();
            ps.close();
        }
    }

    public void updateExisting(Ticket ticket) throws Exception {
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(UPDATE_STATEMENT);
            ps.setString(1, ticket.getQrCode());
            if (ticket.getPurchaseDate() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(ticket.getPurchaseDate()));
            } else {
                ps.setTimestamp(2, null); // Xử lý trường hợp purchaseDate là null nếu cần thiết
            }
            ps.setInt(3, ticket.getOrder().getOrderId());
            ps.setInt(4, ticket.getSeat().getSeatId());
            ps.setInt(5, ticket.getStatus().toInteger());
            ps.setInt(6, ticket.getTicketId());

            ps.executeUpdate();
            ps.close();
        }
    }

    public List<Ticket> findByUserId(int userId) throws Exception {
        List<Ticket> tickets = new ArrayList<>();

        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(SELECT_BY_USER_ID_STATEMENT);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
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
                ticket.setStatus(Ticket.TicketStatus.fromInteger(rs.getInt("status")));
                tickets.add(ticket);
            }
            rs.close();
            ps.close();
        }

        return tickets;
    }

    public List<Ticket> findByUserId(int userId, int limit, int offset) throws Exception {
        List<Ticket> tickets = new ArrayList<>();

        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(SELECT_BY_USER_ID_WITH_LIMIT);
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            ResultSet rs = ps.executeQuery();
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
                ticket.setStatus(Ticket.TicketStatus.fromInteger(rs.getInt("status")));
                tickets.add(ticket);
            }
            rs.close();
            ps.close();
        }

        return tickets;
    }

    public int countByUserId(int userId) throws Exception {
        int count = 0;
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(COUNT_BY_USER_ID_STATEMENT);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            ps.close();
        }
        return count;
    }

    public Ticket findByQrCode(String qrCode) throws Exception {
        Ticket ticket = null;
        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(SELECT_BY_QR_CODE_STATEMENT);
            ps.setString(1, qrCode);
            ResultSet rs = ps.executeQuery();
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
                ticket.setStatus(Ticket.TicketStatus.fromInteger(rs.getInt("status")));
            }
            rs.close();
            ps.close();
        }
        return ticket;
    }

    public int findOrganizerIdByOrderId(int orderId) {
        String sql = "SELECT e.organizer_id " +
                "FROM `Order` o " +
                "INNER JOIN Ticket t ON o.order_id = t.order_id " +
                "INNER JOIN Seat s ON t.seat_id = s.seat_id " +
                "INNER JOIN `Row` r ON s.row_id = r.row_id " +
                "INNER JOIN Area a ON r.area_id = a.area_id " +
                "INNER JOIN Event e ON a.event_id = e.event_id " +
                "WHERE o.order_id = ?";

        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("organizer_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving organizer_id by order ID", e);
        }
        return -1; // Return -1 or throw an exception if organizer_id not found
    }


    public int findOrderIdByTicketId(int ticketId) {
        String sql = "SELECT o.order_id " +
                "FROM Ticket t " +
                "INNER JOIN `Order` o ON t.order_id = o.order_id " +
                "WHERE t.ticket_id = ?";

        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, ticketId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("order_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving order_id by ticket ID", e);
        }
        return -1; // Return -1 or throw an exception if order_id not found
    }

    public int findRatingIdByOrderId(int orderId) {
        String sql = "SELECT rating_id FROM Rating WHERE order_id = ?";

        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("rating_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving rating_id by order ID", e);
        }
        return -1; // Return -1 or throw an exception if rating_id not found
    }


}