package com.example.VieTicketSystem.model.repo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.example.VieTicketSystem.model.entity.Seat;
import org.springframework.stereotype.Repository;

@Repository
public class SeatRepo {

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Seat WHERE seat_id = ?";
    private static final String SELECT_BY_EVENT_ID_SQL = """
            SELECT * FROM Seat
            JOIN VieTicket1.Row R ON R.row_id = Seat.row_id
            JOIN VieTicket1.Area A ON R.area_id = A.area_id
            WHERE event_id = 5""";
    private static final String SELECT_BY_ROW_ID_SQL = "SELECT * FROM Seat WHERE row_id = ?";
    private static final String UPDATE_SQL = "UPDATE Seat SET is_taken = ? WHERE seat_id = ?";
    private static final String SELECT_PRICE_SQL = "SELECT ticket_price FROM Seat WHERE seat_id = ?";
    private static final String UPDATE_IN_BULK_SQL = "UPDATE Seat SET is_taken = ? WHERE seat_id = ?";
    private static final String COUNT_SEATS_WITH_STATUS = "SELECT COUNT(*) FROM Seat S JOIN `Row` R ON S.row_id = R.row_id JOIN Area A ON R.area_id = A.area_id WHERE A.event_id = ? AND is_taken = ?";
    private final RowRepo rowRepo;

    public SeatRepo(RowRepo rowRepo) {
        this.rowRepo = rowRepo;
    }

    public List<Integer> findAvailableSeatsByEventId(int eventId) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT seat_id FROM Seat S JOIN `Row` R ON S.row_id = R.row_id JOIN Area A ON R.area_id = A.area_id WHERE A.event_id = ? AND is_taken = FALSE");
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();
        List<Integer> seats = new ArrayList<>();
        while (rs.next()) {
            seats.add(rs.getInt("seat_id"));
        }
        rs.close();
        ps.close();
        connection.close();
        return seats;
    }

    public void updateSeats(List<Integer> seatIds, Seat.TakenStatus isTaken) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(UPDATE_IN_BULK_SQL);

        for (Integer seatId : seatIds) {
            ps.setInt(1, isTaken.toInteger());
            ps.setInt(2, seatId);
            ps.addBatch();
        }

        ps.executeBatch();
        ps.close();
        connection.close();
    }

    public float getPrice(int seatId) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(SELECT_PRICE_SQL);
        ps.setInt(1, seatId);
        ResultSet rs = ps.executeQuery();
        float price = 0;
        if (rs.next()) {
            price = rs.getFloat("ticket_price");
        }
        rs.close();
        ps.close();
        connection.close();
        return price;
    }

    public void updateSeat(int seatId, boolean isTaken) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
        ps.setBoolean(1, isTaken);
        ps.setInt(2, seatId);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public Seat findById(int id) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_SQL);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Seat seat = null;
        if (rs.next()) {
            seat = new Seat();
            seat.setSeatId(rs.getInt("seat_id"));
            seat.setNumber(rs.getString("number"));
            seat.setTicketPrice(rs.getFloat("ticket_price"));
            seat.setTaken(Seat.TakenStatus.fromInteger(rs.getInt("is_taken")));
            seat.setRow(rowRepo.findById(rs.getInt("row_id")));
        }
        rs.close();
        ps.close();
        connection.close();
        return seat;
    }

    public void addSeat(String seatNumber, String ticketPrice, int rowId) throws SQLException {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Seat (number, ticket_price, is_taken, row_id) VALUES (?, ?, ?, ?)");

        ps.setString(1, seatNumber);
        ps.setFloat(2, Float.parseFloat(ticketPrice));
        ps.setInt(3, Seat.TakenStatus.AVAILABLE.toInteger());
        ps.setInt(4, rowId);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }
    public void addSeats(List<Seat> seats) throws SQLException {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement("INSERT INTO Seat (number, ticket_price, is_taken, row_id) VALUES (?, ?, ?, ?)");
    
        for (Seat seat : seats) {
            ps.setString(1, seat.getNumber());
            ps.setFloat(2, seat.getTicketPrice());
            ps.setInt(3, Seat.TakenStatus.AVAILABLE.toInteger());
            ps.setInt(4, seat.getRow().getRowId());
            ps.addBatch();
        }
    
        ps.executeBatch();
        ps.close();
        connection.close();
    }

    public List<Seat> findByEventId(int eventId) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(SELECT_BY_EVENT_ID_SQL);
        ResultSet rs = ps.executeQuery();
        List<Seat> seats = new ArrayList<>();
        while (rs.next()) {
            Seat seat = new Seat();
            seat.setSeatId(rs.getInt("seat_id"));
            seat.setNumber(rs.getString("number"));
            seat.setTicketPrice(rs.getFloat("ticket_price"));
            seat.setTaken(Seat.TakenStatus.fromInteger(rs.getInt("is_taken")));
            seat.setRow(rowRepo.findById(rs.getInt("row_id")));
            seats.add(seat);
        }
        rs.close();
        ps.close();
        connection.close();
        return seats;
    }

    public List<Seat> findByRowId(int areaId) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(SELECT_BY_ROW_ID_SQL);
        ps.setInt(1, areaId);
        ResultSet rs = ps.executeQuery();
        List<Seat> seats = new ArrayList<>();
        while (rs.next()) {
            Seat seat = new Seat();
            seat.setSeatId(rs.getInt("seat_id"));
            seat.setNumber(rs.getString("number"));
            seat.setTicketPrice(rs.getFloat("ticket_price"));
            seat.setTaken(Seat.TakenStatus.fromInteger(rs.getInt("is_taken")));
            seat.setRow(rowRepo.findById(rs.getInt("row_id")));
            seats.add(seat);
        }
        rs.close();
        ps.close();
        connection.close();
        return seats;
    }

    public int getAvailableSeatsCount(int eventId, int status) throws Exception {
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(COUNT_SEATS_WITH_STATUS);
        ps.setInt(1, eventId);
        ps.setInt(2, status);
        ResultSet rs = ps.executeQuery();
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        connection.close();
        return count;
    }
}