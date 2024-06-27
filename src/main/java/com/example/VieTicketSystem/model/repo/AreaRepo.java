package com.example.VieTicketSystem.model.repo;

import java.sql.*;

import com.example.VieTicketSystem.model.entity.Area;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Repository;

@Repository
public class AreaRepo {
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Area WHERE area_id = ?";
    private static final String SELECT_BY_EVENT_ID_SQL = "SELECT * FROM Area WHERE event_id = ?";
    private final EventRepo eventRepo;

    public AreaRepo(EventRepo eventRepo) {
        this.eventRepo = eventRepo;
    }

    public List<Area> findByEventId(int eventId) throws Exception {
        try {
            Connection connection = ConnectionPoolManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_EVENT_ID_SQL);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            List<Area> areas = new java.util.ArrayList<>();
            while (rs.next()) {
                Area area = new Area();
                area.setAreaId(rs.getInt("area_id"));
                area.setName(rs.getString("name"));
                area.setTotalTickets(rs.getInt("total_tickets"));
                area.setEvent(eventRepo.findById(rs.getInt("event_id")));
                area.setTicketPrice(rs.getFloat("ticket_price"));
                areas.add(area);
            }
            rs.close();
            ps.close();
            connection.close();
            return areas;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Float> getTicketPricesByEventId(int eventId) throws Exception {
        List<Float> ticketPrices = new ArrayList<>();
        Connection connection = ConnectionPoolManager.getConnection();
        String sql = "SELECT ticket_price FROM Area WHERE event_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ticketPrices.add(rs.getFloat("ticket_price"));
        }
        rs.close();
        ps.close();
        connection.close();
        return ticketPrices;
    }

    public Area findById(int id) throws Exception {
        try {
            Connection connection = ConnectionPoolManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Area area = null;
            if (rs.next()) {
                area = new Area();
                area.setAreaId(rs.getInt("area_id"));
                area.setName(rs.getString("name"));
                area.setTotalTickets(rs.getInt("total_tickets"));
                area.setEvent(eventRepo.findById(rs.getInt("event_id")));
                area.setTicketPrice(rs.getFloat("ticket_price"));
            }
            rs.close();
            ps.close();
            connection.close();
            return area;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addArea(String areaName, int totalTicket, int eventId, String ticketPrice, int seatMapId)
            throws ClassNotFoundException, SQLException, ParseException {
        NumberFormat format = NumberFormat.getInstance(Locale.US);

        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO Area (event_id, name, total_tickets, ticket_price, seat_map_id) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, eventId);
        ps.setString(2, areaName);
        ps.setInt(3, totalTicket);
        ps.setFloat(4, format.parse(ticketPrice).floatValue());
        ps.setInt(5, seatMapId);
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public List<Area> addAreas(List<Area> areas) throws SQLException {
        Connection connection = ConnectionPoolManager.getConnection();
        Statement statement = connection.createStatement();

        StringBuilder sql = new StringBuilder(
                "INSERT INTO `Area` (name, total_tickets, event_id, ticket_price, seat_map_id) VALUES ");
        for (int i = 0; i < areas.size(); i++) {
            Area area = areas.get(i);

            // Escape single quotes in area name
            String escapedAreaName = area.getName().replace("'", "''");

            sql.append(String.format("('%s', %d, %d, '%s', %d)",
                    escapedAreaName,
                    area.getTotalTickets(),
                    area.getEvent().getEventId(),
                    area.getTicketPrice(),
                    area.getSeatMapId()));
            if (i < areas.size() - 1) {
                sql.append(", ");
            }
        }

        // Execute the statement with careful handling of SQL string
        statement.execute(sql.toString(), Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = statement.getGeneratedKeys();

        int index = 0;
        while (rs.next()) {
            int id = rs.getInt(1);
            areas.get(index).setAreaId(id);
            index++;
        }

        rs.close();
        statement.close();
        connection.close();

        return areas;
    }

    public int getIdAreaEventId(int eventId) throws ClassNotFoundException, SQLException {
        int areaId = -1; // Giá trị mặc định khi không tìm thấy khu vực

        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT area_id FROM Area WHERE event_id = ? ");
        ps.setInt(1, eventId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            areaId = rs.getInt("area_id");
        }

        rs.close();
        ps.close();
        connection.close();

        return areaId;
    }

    public int getIdAreaEventIdAndName(int eventId, String name) throws ClassNotFoundException, SQLException {
        int areaId = -1; // Giá trị mặc định khi không tìm thấy khu vực

        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT area_id FROM Area WHERE event_id = ? and name = ? ");
        ps.setInt(1, eventId);
        ps.setString(2, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            areaId = rs.getInt("area_id");
        }

        rs.close();
        ps.close();
        connection.close();

        return areaId;
    }

}
