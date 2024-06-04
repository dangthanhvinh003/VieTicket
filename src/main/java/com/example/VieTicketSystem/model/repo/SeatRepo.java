package com.example.VieTicketSystem.model.repo;

import java.sql.*;

import com.example.VieTicketSystem.model.entity.Seat;
import org.springframework.stereotype.Repository;

@Repository
public class SeatRepo {

        private static final String SELECT_BY_ID_SQL = "SELECT * FROM Seat WHERE seat_id = ?";
        private final RowRepo rowRepo;

        public SeatRepo(RowRepo rowRepo) {
                this.rowRepo = rowRepo;
        }

        public Seat findById(int id) throws Exception {
                Class.forName(Baseconnection.nameClass);
                Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                                Baseconnection.password);
                PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_SQL);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                Seat seat = null;
                if (rs.next()) {
                        seat = new Seat();
                        seat.setSeatId(rs.getInt("seat_id"));
                        seat.setNumber(rs.getString("number"));
                        seat.setTicketPrice(rs.getFloat("ticket_price"));
                        seat.setTaken(rs.getBoolean("is_taken"));
                        seat.setRow(rowRepo.findById(rs.getInt("row_id")));
                }
                rs.close();
                ps.close();
                connection.close();
                return seat;
        }

        public void addSeat(String seatNumber, String ticketPrice, int rowId)
                        throws ClassNotFoundException, SQLException {

                Class.forName(Baseconnection.nameClass);
                Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                                Baseconnection.password);
                PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO Seat (number, ticket_price, is_taken, row_id) VALUES (?, ?, ?, ?)");

                ps.setString(1, seatNumber);
                ps.setFloat(2, Float.parseFloat(ticketPrice));
                ps.setBoolean(3, false);
                ps.setInt(4, rowId);
                ps.executeUpdate();
                ps.close();
        }
}
