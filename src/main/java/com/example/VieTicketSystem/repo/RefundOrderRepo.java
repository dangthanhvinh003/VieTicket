package com.example.VieTicketSystem.repo;

import com.example.VieTicketSystem.model.entity.RefundOrder;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class RefundOrderRepo {

    private static final String SAVE_NEW_SQL = "INSERT INTO RefundOrder (created_on, order_id, status) VALUES (?, ?, ?)";
    private static final String UPDATE_STATUS_SQL = "UPDATE RefundOrder SET status = ? WHERE order_id = ?";
    private static final String FIND_BY_ORDER_ID_SQL = "SELECT * FROM RefundOrder WHERE order_id = ?";
    private final OrderRepo orderRepo;

    public RefundOrderRepo(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    public void save(RefundOrder refundOrder) throws SQLException {
        try (Connection connection = ConnectionPoolManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(SAVE_NEW_SQL)) {
                statement.setTimestamp(1, Timestamp.valueOf(refundOrder.getCreatedOn()));
                statement.setInt(2, refundOrder.getOrder().getOrderId());
                statement.setInt(3, refundOrder.getStatus().toInteger());
                statement.executeUpdate();
            }
        }
    }

    public void updateStatus(RefundOrder refundOrder) throws SQLException {
        try (Connection connection = ConnectionPoolManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {
                statement.setInt(1, refundOrder.getStatus().toInteger());
                statement.setInt(2, refundOrder.getOrder().getOrderId());
                statement.executeUpdate();
            }
        }
    }

    public RefundOrder findByOrderId(int orderId) throws Exception {
        try (Connection connection = ConnectionPoolManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ORDER_ID_SQL)) {
                statement.setInt(1, orderId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new RefundOrder(
                            resultSet.getTimestamp("created_on").toLocalDateTime(),
                            orderRepo.findById(resultSet.getInt("order_id")),
                            RefundOrder.RefundStatus.fromInteger(resultSet.getInt("status"))
                        );
                    }
                }
            }
        }
        return null;
    }
}
