package com.example.VieTicketSystem.model.repo;

import com.example.VieTicketSystem.model.entity.Order;
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

    public List<Order> findAllByUserId(int userId, int page, int size) {
        // TODO: Implement findAll by user id (with pagination)
        return null;
    }
}
