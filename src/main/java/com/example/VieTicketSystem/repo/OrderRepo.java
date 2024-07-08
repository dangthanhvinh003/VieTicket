package com.example.VieTicketSystem.repo;

import com.example.VieTicketSystem.model.entity.Order;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.entity.Rating;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepo {

    private static final String INSERT_ORDER_SQL = "INSERT INTO `Order`(date, total, user_id, vnpay_data, status) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_ORDER_SQL = "SELECT * FROM `Order` WHERE order_id = ?";
    private static final String SELECT_ORDER_BY_TXN_REF_SQL = "SELECT * FROM `Order` WHERE JSON_EXTRACT(vnpay_data, '$.vnp_TxnRef') = ?";
    private static final String UPDATE_ORDER_STATUS_SQL = "UPDATE `Order` SET status = ? WHERE order_id = ?";
    private static final String SELECT_ORDERS_BY_STATUS_SQL = "SELECT * FROM `Order` WHERE status = ?";
    private static final String SELECT_BY_USER_ID_WITH_LIMIT = "SELECT order_id, date, total, status FROM `Order` WHERE user_id = ? ORDER BY order_id DESC LIMIT ? OFFSET ?";
    private static final String COUNT_BY_USER_ID = "SELECT COUNT(*) FROM `Order` WHERE user_id = ?;";
    private final UserRepo userRepo;

    public OrderRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<Order> findByStatus(Order.PaymentStatus status) throws Exception {
        List<Order> orders = new ArrayList<>();


        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(SELECT_ORDERS_BY_STATUS_SQL);
            ps.setInt(1, status.toInteger());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setDate(rs.getTimestamp("date").toLocalDateTime());
                order.setTotal(rs.getLong("total"));
                order.setUser(userRepo.findById(rs.getInt("user_id")));
                order.setVnpayData(rs.getString("vnpay_data"));
                order.setStatus(Order.PaymentStatus.fromInteger(rs.getInt("status")));
                orders.add(order);
            }

            rs.close();
            ps.close();
        }

        return orders;
    }

    public void updateStatus(int orderId, Order.PaymentStatus status) throws Exception {
        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_ORDER_STATUS_SQL)) {

            ps.setInt(1, status.toInteger());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public Order findByTxnRef(String txnRef) throws Exception {

        Order order = null;

        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ORDER_BY_TXN_REF_SQL)) {

            ps.setString(1, txnRef);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setDate(rs.getTimestamp("date").toLocalDateTime());
                order.setTotal(rs.getLong("total"));
                order.setUser(userRepo.findById(rs.getInt("user_id")));
                order.setVnpayData(rs.getString("vnpay_data"));
                order.setStatus(Order.PaymentStatus.fromInteger(rs.getInt("status")));
            }
            rs.close();
        }

        return order;
    }

    public int save(Order order) throws Exception {

        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_ORDER_SQL, Statement.RETURN_GENERATED_KEYS)) {

            if (order.getDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(order.getDate()));
            } else {
                ps.setTimestamp(1, null); // Handle the case where order.getDate() is null if necessary
            }
            ps.setLong(2, order.getTotal());
            ps.setInt(3, order.getUser().getUserId());
            ps.setString(4, order.getVnpayData());
            ps.setInt(5, order.getStatus().toInteger());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated order ID
            } else {
                throw new Exception("Failed to save order, no ID obtained.");
            }
        }
    }

    public Order findById(int id) throws Exception {
        Order order = null;
        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ORDER_SQL)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setDate(rs.getTimestamp("date").toLocalDateTime());
                order.setTotal(rs.getLong("total"));
                order.setUser(userRepo.findById(rs.getInt("user_id")));
                order.setVnpayData(rs.getString("vnpay_data"));
                order.setStatus(Order.PaymentStatus.fromInteger(rs.getInt("status")));
            }
            rs.close();
        }

        return order;
    }

    public List<Order> findByUserId(int userId, int limit, int offset) throws SQLException {
        List<Order> orders = new ArrayList<>();

        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(SELECT_BY_USER_ID_WITH_LIMIT);
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                Timestamp timestamp = rs.getTimestamp("date");
                if (timestamp == null) {
                    order.setDate(null);
                } else {
                    order.setDate(timestamp.toLocalDateTime());
                }
                order.setTotal(rs.getLong("total"));
                order.setUser(new User() {{ setUserId(userId);}});
                order.setStatus(Order.PaymentStatus.fromInteger(rs.getInt("status")));
                orders.add(order);
            }
        }

        return orders;
    }

    public int countByUserId(int userId) throws Exception {

        try (Connection con = ConnectionPoolManager.getConnection()) {
            PreparedStatement ps = con.prepareStatement(COUNT_BY_USER_ID);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
            ps.close();
        }
        return Integer.MIN_VALUE;
    }

    //Rating Organize 
    public void submitRating(int star, int organizer_id, int order_id) {
        String sql = "INSERT INTO Rating (star, organizer_id, order_id) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, star);
            preparedStatement.setInt(2, organizer_id);
            preparedStatement.setInt(3, order_id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            // Handle exceptions appropriately
            throw new RuntimeException("Error saving rating to the database", e);
        }
    }
}
