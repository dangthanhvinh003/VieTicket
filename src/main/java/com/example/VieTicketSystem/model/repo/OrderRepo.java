package com.example.VieTicketSystem.model.repo;

import com.example.VieTicketSystem.model.entity.Order;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class OrderRepo {

    private static final String INSERT_ORDER_SQL = "INSERT INTO `Order`(date, total, user_id, vnpay_data, status) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_ORDER_SQL = "SELECT * FROM `Order` WHERE order_id = ?";
    private static final String SELECT_ORDER_BY_TXN_REF_SQL = "SELECT * FROM `Order` WHERE JSON_EXTRACT(vnpay_data, '$.vnp_TxnRef') = ?";
    private static final String UPDATE_ORDER_STATUS_SQL = "UPDATE `Order` SET status = ? WHERE order_id = ?";
    private final UserRepo userRepo;

    public OrderRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
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

        Class.forName(Baseconnection.nameClass);
        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ORDER_BY_TXN_REF_SQL)) {

            ps.setString(1, txnRef);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setDate(rs.getTimestamp("date").toLocalDateTime());
                order.setTotal(rs.getInt("total"));
                order.setUser(userRepo.findById(rs.getInt("user_id")));
                order.setVnpayData(rs.getString("vnpay_data"));
                order.setStatus(Order.PaymentStatus.fromInteger(rs.getInt("status")));
                return order;
            } else {
                return null;
            }
        }
    }

    public int save(Order order) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_ORDER_SQL, Statement.RETURN_GENERATED_KEYS)) {

            if (order.getDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(order.getDate()));
            } else {
                ps.setTimestamp(1, null); // Handle the case where order.getDate() is null if necessary
            }
            ps.setInt(2, order.getTotal());
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
        Class.forName(Baseconnection.nameClass);
        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ORDER_SQL)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setDate(rs.getTimestamp("date").toLocalDateTime());
                order.setTotal(rs.getInt("total"));
                order.setUser(userRepo.findById(rs.getInt("user_id")));
                order.setVnpayData(rs.getString("vnpay_data"));
                order.setStatus(Order.PaymentStatus.fromInteger(rs.getInt("status")));
                return order;
            } else {
                return null;
            }
        }
    }
}
