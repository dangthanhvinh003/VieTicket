package com.example.VieTicketSystem.model.repo;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;

import com.example.VieTicketSystem.model.entity.Area;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.SeatMap;

@Repository
public class EventRepo {

    static {
        try {
            Class.forName(Baseconnection.nameClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL driver", e);
        }
    }
    @Autowired
    OrganizerRepo organizerRepo = new OrganizerRepo();

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Event");
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Event event = new Event();
                event.setEventId(resultSet.getInt("event_id"));
                event.setName(resultSet.getString("name"));
                event.setDescription(resultSet.getString("description"));
                event.setStartDate(resultSet.getDate("start_date"));
                event.setLocation(resultSet.getString("location"));
                event.setType(resultSet.getString("type"));
                event.setTicketSaleDate(resultSet.getDate("ticket_sale_date"));
                event.setEndDate(resultSet.getDate("end_date"));
                event.setPoster(resultSet.getString("poster"));
                event.setBanner(resultSet.getString("banner"));
                event.setApprove(resultSet.getBoolean("is_approve"));
                // Set the organizer if applicable
                events.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return events;
    }

    public Event getEventById(int eventid) {
        Event event = null;
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Event WHERE event_id = ?");) {

            statement.setInt(1, eventid); // Truyền tên sự kiện cụ thể vào câu lệnh SQL

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    event = new Event();
                    event.setEventId(resultSet.getInt("event_id"));
                    event.setName(resultSet.getString("name"));
                    event.setDescription(resultSet.getString("description"));
                    event.setStartDate(resultSet.getDate("start_date"));
                    event.setLocation(resultSet.getString("location"));
                    event.setType(resultSet.getString("type"));
                    event.setTicketSaleDate(resultSet.getDate("ticket_sale_date"));
                    event.setEndDate(resultSet.getDate("end_date"));
                    event.setPoster(resultSet.getString("poster"));
                    event.setBanner(resultSet.getString("banner"));
                    event.setApprove(resultSet.getBoolean("is_approve"));
                    // Set the organizer if applicable
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public int addEvent(String name, String description, Date startDate, String location, String type,
            Date ticketSaleDate, Date endDate, int organizerId, String poster, String banner)
            throws ClassNotFoundException, SQLException {

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);

        // Sử dụng tùy chọn RETURN_GENERATED_KEYS để lấy khóa tự động tăng
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO Event (name, description, start_date, location, type, ticket_sale_date, end_date, organizer_id, poster, banner, is_approve) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, name);
        ps.setString(2, description);
        ps.setDate(3, startDate);
        ps.setString(4, location);
        ps.setString(5, type);
        ps.setDate(6, ticketSaleDate);
        ps.setDate(7, endDate);
        ps.setInt(8, organizerId);
        ps.setString(9, poster);
        ps.setString(10, banner);
        ps.setBoolean(11, false);
        ps.executeUpdate();

        // Lấy khóa tự động tăng vừa được tạo
        ResultSet rs = ps.getGeneratedKeys();
        int eventId = 0;
        if (rs.next()) {
            eventId = rs.getInt(1);
        }

        rs.close();
        ps.close();
        connection.close();

        return eventId;
    }

    public int updateEvent(int eventId, String name, String description, Date startDate, String location, String type,
            Date ticketSaleDate, Date endDate, int organizerId, String poster, String banner)
            throws ClassNotFoundException, SQLException {

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);

        // Câu lệnh SQL để cập nhật sự kiện
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE Event SET name = ?, description = ?, start_date = ?, location = ?, type = ?, " +
                        "ticket_sale_date = ?, end_date = ?, organizer_id = ?, poster = ?, banner = ?, is_approve = ? "
                        +
                        "WHERE event_id = ?");

        ps.setString(1, name);
        ps.setString(2, description);
        ps.setDate(3, startDate);
        ps.setString(4, location);
        ps.setString(5, type);
        ps.setDate(6, ticketSaleDate);
        ps.setDate(7, endDate);
        ps.setInt(8, organizerId);
        ps.setString(9, poster);
        ps.setString(10, banner);
        ps.setBoolean(11, false); // Giả sử trạng thái phê duyệt không thay đổi
        ps.setInt(12, eventId);

        int rowsUpdated = ps.executeUpdate();

        ps.close();
        connection.close();

        return rowsUpdated;
    }

    public ArrayList<Event> getAllEventsByOrganizerId(int organizerId) {
        ArrayList<Event> events = new ArrayList<>();
        String query = "SELECT * FROM Event WHERE organizer_id = ?";

        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, organizerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Event event = new Event();
                    event.setEventId(resultSet.getInt("event_id"));
                    event.setName(resultSet.getString("name"));
                    event.setDescription(resultSet.getString("description"));
                    event.setStartDate(resultSet.getDate("start_date"));
                    event.setLocation(resultSet.getString("location"));
                    event.setType(resultSet.getString("type"));
                    event.setTicketSaleDate(resultSet.getDate("ticket_sale_date"));
                    event.setEndDate(resultSet.getDate("end_date"));
                    event.setPoster(resultSet.getString("poster"));
                    event.setBanner(resultSet.getString("banner"));
                    event.setApprove(resultSet.getBoolean("is_approve"));
                    event.setAreas(getAreasByEventId(event.getEventId()));

                    // Lấy ảnh sơ đồ chỗ ngồi
                    event.setSeatMap((getSeatMapDetailsByEventId(event.getEventId())));
                    events.add(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return events;
    }

    public ArrayList<Event> searchEventByNameAndOrganizerId(String name, int organizerId) {
        ArrayList<Event> events = new ArrayList<>();
        String query = "SELECT * FROM Event WHERE organizer_id = ? AND name LIKE ?";

        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, organizerId);
            statement.setString(2, "%" + name + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Event event = new Event();
                    event.setEventId(resultSet.getInt("event_id"));
                    event.setName(resultSet.getString("name"));
                    event.setDescription(resultSet.getString("description"));
                    event.setStartDate(resultSet.getDate("start_date"));
                    event.setLocation(resultSet.getString("location"));
                    event.setType(resultSet.getString("type"));
                    event.setTicketSaleDate(resultSet.getDate("ticket_sale_date"));
                    event.setEndDate(resultSet.getDate("end_date"));
                    event.setPoster(resultSet.getString("poster"));
                    event.setBanner(resultSet.getString("banner"));
                    event.setApprove(resultSet.getBoolean("is_approve"));
                    // Set the organizer_id if applicable
                    event.setAreas(getAreasByEventId(event.getEventId()));

                    // Lấy ảnh sơ đồ chỗ ngồi
                    event.setSeatMap((getSeatMapDetailsByEventId(event.getEventId())));

                    events.add(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return events;
    }

    private List<Area> getAreasByEventId(int eventId) throws Exception {
        List<Area> areas = new ArrayList<>();
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        String query = "SELECT * FROM Area WHERE event_id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Area area = new Area();
            area.setAreaId(rs.getInt("area_id"));
            area.setName(rs.getString("name"));
            area.setTicketPrice(rs.getFloat("ticket_price"));
            areas.add(area);
        }

        rs.close();
        ps.close();
        con.close();

        return areas;
    }

    private SeatMap getSeatMapDetailsByEventId(int eventId) throws Exception {
        SeatMap seatMapDetails = null;
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        String query = "SELECT img, name FROM SeatMap WHERE event_id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String seatMapImage = rs.getString("img");
            String seatMapName = rs.getString("name");
            seatMapDetails = new SeatMap(seatMapName, seatMapImage);
        }

        rs.close();
        ps.close();
        con.close();

        return seatMapDetails;
    }
}
