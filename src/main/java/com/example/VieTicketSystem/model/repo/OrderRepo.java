package com.example.VieTicketSystem.model.repo;

import com.example.VieTicketSystem.model.entity.Order;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class OrderRepo {

    private static final String INSERT_ORDER_SQL = "INSERT INTO `Order`(date, total, user_id, vnpay_data) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ORDER_SQL = "SELECT * FROM `Order` WHERE order_id = ?";
    private final UserRepo userRepo;

    public OrderRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public void save(Order order) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password)) {
            PreparedStatement ps = con.prepareStatement(INSERT_ORDER_SQL);
            ps.setDate(1, order.getDate());
            ps.setInt(2, order.getTotal());
            ps.setInt(3, order.getUser().getUserId());
            ps.setString(4, order.getVnpayData());
            ps.executeUpdate();
        }
    }

    public Order findById(int id) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password)) {
            PreparedStatement ps = con.prepareStatement(SELECT_ORDER_SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setDate(rs.getDate("date"));
                order.setTotal(rs.getInt("total"));
                order.setUser(userRepo.findById(rs.getInt("user_id")));
                order.setVnpayData(rs.getString("vnpay_data"));
                return order;
            } else {
                return null;
            }
        }
    }
}
