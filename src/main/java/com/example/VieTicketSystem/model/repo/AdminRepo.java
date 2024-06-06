package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Area;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.entity.SeatMap;

@Repository
public class AdminRepo {
        @Autowired
        OrganizerRepo organizerRepo = new OrganizerRepo();

        public ArrayList<Organizer> viewAllListAprrove() throws ClassNotFoundException, SQLException {
                ArrayList<Organizer> organizersList = new ArrayList<>();
                Class.forName(Baseconnection.nameClass);
                Connection con = ConnectionPoolManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                                "SELECT " +
                                                "o.organizer_id, " +
                                                "u.full_name, " +
                                                "u.phone, " +
                                                "u.dob, " +
                                                "u.gender, " +
                                                "u.avatar, " +
                                                "u.role, " +
                                                "u.email, " +
                                                "o.founded_date, " +
                                                "o.website, " +
                                                "o.organizer_addr, " +
                                                "o.organizer_type " +
                                                "FROM " +
                                                "Organizer o " +
                                                "JOIN " +
                                                "User u ON o.organizer_id = u.user_id " +
                                                "WHERE " +
                                                "o.is_active = 0");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                        Organizer organizer = new Organizer();
                        organizer.setUserId(rs.getInt("organizer_id"));
                        organizer.setFullName(rs.getString("full_name"));
                        organizer.setPhone(rs.getString("phone"));
                        organizer.setDob(rs.getDate("dob"));
                        organizer.setGender(rs.getString("gender").charAt(0));
                        organizer.setAvatar(rs.getString("avatar"));
                        organizer.setRole(rs.getString("role").charAt(0));
                        organizer.setEmail(rs.getString("email"));
                        organizer.setFoundedDate(rs.getDate("founded_date"));
                        organizer.setWebsite(rs.getString("website"));
                        organizer.setOrganizerAddr(rs.getString("organizer_addr"));
                        organizer.setOrganizerType(rs.getString("organizer_type"));
                        organizersList.add(organizer);
                }
                return organizersList;

        }

        public void approveOrganizers(int organizer_id)
                        throws Exception {
                Class.forName(Baseconnection.nameClass);
                Connection con = ConnectionPoolManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                                "UPDATE Organizer SET is_active = 1 WHERE organizer_id = ?");
                ps.setInt(1, organizer_id);

                ps.executeUpdate();
                ps.close();

        }

        public ArrayList<Event> viewAllListApproveEvent() throws Exception {
                ArrayList<Event> eventList = new ArrayList<>();
                Class.forName(Baseconnection.nameClass);
                Connection con = ConnectionPoolManager.getConnection();

                String query = "SELECT * FROM Event WHERE is_approve = 0";
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                        Event event = new Event();
                        event.setEventId(rs.getInt("event_id"));
                        event.setName(rs.getString("name"));
                        event.setDescription(rs.getString("description"));

                        // Sử dụng getTimestamp() và chuyển đổi thành LocalDateTime
                        Timestamp startTimestamp = rs.getTimestamp("start_date");
                        if (startTimestamp != null) {
                                event.setStartDate(startTimestamp.toLocalDateTime());
                        }

                        event.setLocation(rs.getString("location"));
                        event.setType(rs.getString("type"));

                        // Sử dụng getTimestamp() và chuyển đổi thành LocalDateTime
                        Timestamp ticketSaleTimestamp = rs.getTimestamp("ticket_sale_date");
                        if (ticketSaleTimestamp != null) {
                                event.setTicketSaleDate(ticketSaleTimestamp.toLocalDateTime());
                        }

                        // Sử dụng getTimestamp() và chuyển đổi thành LocalDateTime
                        Timestamp endTimestamp = rs.getTimestamp("end_date");
                        if (endTimestamp != null) {
                                event.setEndDate(endTimestamp.toLocalDateTime());
                        }

                        event.setPoster(rs.getString("poster"));
                        event.setBanner(rs.getString("banner"));
                        event.setApproved(rs.getInt("is_approve"));

                        // Giả sử bạn có một phương thức để tìm organizer theo ID
                        event.setOrganizer(organizerRepo.findById(rs.getInt("organizer_id")));

                        // Lấy danh sách các khu vực và giá tiền
                        event.setAreas(getAreasByEventId(event.getEventId()));

                        // Lấy ảnh sơ đồ chỗ ngồi
                        event.setSeatMap((getSeatMapDetailsByEventId(event.getEventId())));

                        eventList.add(event);
                }

                rs.close();
                ps.close();
                con.close();

                return eventList;
        }

        public ArrayList<Event> viewAllListRejectEvent() throws Exception {
                ArrayList<Event> eventList = new ArrayList<>();
                Class.forName(Baseconnection.nameClass);
                Connection con = ConnectionPoolManager.getConnection();

                String query = "SELECT * FROM Event WHERE is_approve = 3";
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                        Event event = new Event();
                        event.setEventId(rs.getInt("event_id"));
                        event.setName(rs.getString("name"));
                        event.setDescription(rs.getString("description"));

                        // Sử dụng getTimestamp() và chuyển đổi thành LocalDateTime
                        Timestamp startTimestamp = rs.getTimestamp("start_date");
                        if (startTimestamp != null) {
                                event.setStartDate(startTimestamp.toLocalDateTime());
                        }

                        event.setLocation(rs.getString("location"));
                        event.setType(rs.getString("type"));

                        // Sử dụng getTimestamp() và chuyển đổi thành LocalDateTime
                        Timestamp ticketSaleTimestamp = rs.getTimestamp("ticket_sale_date");
                        if (ticketSaleTimestamp != null) {
                                event.setTicketSaleDate(ticketSaleTimestamp.toLocalDateTime());
                        }

                        // Sử dụng getTimestamp() và chuyển đổi thành LocalDateTime
                        Timestamp endTimestamp = rs.getTimestamp("end_date");
                        if (endTimestamp != null) {
                                event.setEndDate(endTimestamp.toLocalDateTime());
                        }

                        event.setPoster(rs.getString("poster"));
                        event.setBanner(rs.getString("banner"));
                        event.setApproved(rs.getInt("is_approve"));

                        // Giả sử bạn có một phương thức để tìm organizer theo ID
                        event.setOrganizer(organizerRepo.findById(rs.getInt("organizer_id")));

                        // Lấy danh sách các khu vực và giá tiền
                        event.setAreas(getAreasByEventId(event.getEventId()));

                        // Lấy ảnh sơ đồ chỗ ngồi
                        event.setSeatMap((getSeatMapDetailsByEventId(event.getEventId())));

                        eventList.add(event);
                }

                rs.close();
                ps.close();
                con.close();

                return eventList;
        }

        private List<Area> getAreasByEventId(int eventId) throws Exception {
                List<Area> areas = new ArrayList<>();
                Connection con = ConnectionPoolManager.getConnection();
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
                Connection con = ConnectionPoolManager.getConnection();
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

        public void approveEvents(int eventId)
                        throws Exception {
                Class.forName(Baseconnection.nameClass);
                Connection con = ConnectionPoolManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                                "UPDATE Event SET is_approve = 1 WHERE event_id = ?");
                ps.setInt(1, eventId);

                ps.executeUpdate();
                ps.close();

        }

        public void rejectEvents(int eventId)
                        throws Exception {
                Class.forName(Baseconnection.nameClass);
                Connection con = ConnectionPoolManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                                "UPDATE Event SET is_approve = 3 WHERE event_id = ?");
                ps.setInt(1, eventId);

                ps.executeUpdate();
                ps.close();

        }

        public void deleteEventById(int eventId) throws Exception {
                Class.forName(Baseconnection.nameClass);
                try (Connection con = ConnectionPoolManager.getConnection()) {
                        String sql = "DELETE FROM Event WHERE event_id = ?";
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setInt(1, eventId);
                                ps.executeUpdate();
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                        throw new Exception("Error deleting event", e);
                }
        }

}
