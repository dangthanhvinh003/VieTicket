package com.example.VieTicketSystem.repo;

import com.example.VieTicketSystem.model.entity.RefundOrder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RefundOrderRepo {

    private static final String SAVE_NEW_SQL = "INSERT INTO RefundOrder (created_on, order_id, status, total) VALUES (?, ?, ?, ?)";
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
                statement.setInt(4, refundOrder.getTotal());
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
                        RefundOrder refundOrder = new RefundOrder();

                        refundOrder.setOrder(orderRepo.findById(resultSet.getInt("order_id")));

                        refundOrder.setStatus(RefundOrder.RefundStatus.fromInteger(resultSet.getInt("status")));
                        refundOrder.setTotal(resultSet.getInt("total"));
                        refundOrder.setActualRefund(resultSet.getInt("actual_refund"));

                        Timestamp timestamp = resultSet.getTimestamp("created_on");
                        refundOrder.setCreatedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        timestamp = resultSet.getTimestamp("approved_on");
                        refundOrder.setApprovedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        timestamp = resultSet.getTimestamp("refunded_on");
                        refundOrder.setRefundedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        return refundOrder;
                    }
                }
            }
        }
        return null;
    }

    public void saveApprovalStatus(RefundOrder refundOrder) throws SQLException {
        String sql = "UPDATE RefundOrder SET status = ?, approved_on = ?, actual_refund = ? WHERE order_id = ?";

        try (Connection connection = ConnectionPoolManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, refundOrder.getStatus().toInteger());
                statement.setTimestamp(2, Timestamp.valueOf(refundOrder.getApprovedOn()));
                statement.setInt(3, refundOrder.getActualRefund());
                statement.setInt(4, refundOrder.getOrder().getOrderId());
                statement.executeUpdate();
            }
        }
    }

    public List<RefundOrder> findUniqueRefundOrders(int status, int eventId) throws Exception {

        List<RefundOrder> refundOrders = new ArrayList<>();

        String sql = "SELECT DISTINCT RO.* " +
                "FROM Event E " +
                "JOIN Area A ON E.event_id = A.event_id " +
                "JOIN `Row` R ON A.area_id = R.area_id " +
                "JOIN Seat S ON R.row_id = S.row_id " +
                "JOIN Ticket T ON S.seat_id = T.seat_id " +
                "JOIN RefundOrder RO ON T.order_id = RO.order_id " +
                "WHERE RO.status = ? AND E.event_id = ?";

        try (Connection connection = ConnectionPoolManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, status);
                statement.setInt(2, eventId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        RefundOrder refundOrder = new RefundOrder();
                        refundOrder.setOrder(orderRepo.findById(resultSet.getInt("order_id")));

                        refundOrder.setStatus(RefundOrder.RefundStatus.fromInteger(resultSet.getInt("status")));
                        refundOrder.setTotal(resultSet.getInt("total"));

                        Timestamp timestamp = resultSet.getTimestamp("created_on");
                        refundOrder.setCreatedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        timestamp = resultSet.getTimestamp("refunded_on");
                        refundOrder.setRefundedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        timestamp = resultSet.getTimestamp("approved_on");
                        refundOrder.setApprovedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        refundOrders.add(refundOrder);
                    }
                }
            }
        }

        return refundOrders;
    }

    public List<RefundOrder> findAllRefundOrders(int status) throws Exception {

        List<RefundOrder> refundOrders = new ArrayList<>();

        String sql = "SELECT DISTINCT RO.* " +
                "FROM Event E " +
                "JOIN Area A ON E.event_id = A.event_id " +
                "JOIN `Row` R ON A.area_id = R.area_id " +
                "JOIN Seat S ON R.row_id = S.row_id " +
                "JOIN Ticket T ON S.seat_id = T.seat_id " +
                "JOIN RefundOrder RO ON T.order_id = RO.order_id " +
                "WHERE RO.status = ?";

        try (Connection connection = ConnectionPoolManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, status);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        RefundOrder refundOrder = new RefundOrder();
                        refundOrder.setOrder(orderRepo.findById(resultSet.getInt("order_id")));

                        refundOrder.setStatus(RefundOrder.RefundStatus.fromInteger(resultSet.getInt("status")));
                        refundOrder.setTotal(resultSet.getInt("total"));

                        Timestamp timestamp = resultSet.getTimestamp("created_on");
                        refundOrder.setCreatedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        timestamp = resultSet.getTimestamp("refunded_on");
                        refundOrder.setRefundedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        timestamp = resultSet.getTimestamp("approved_on");
                        refundOrder.setApprovedOn(timestamp == null ? null : timestamp.toLocalDateTime());

                        refundOrders.add(refundOrder);
                    }
                }
            }
        }

        return refundOrders;
    }
}
