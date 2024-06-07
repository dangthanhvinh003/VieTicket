package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.AdminStatistics;
import com.example.VieTicketSystem.model.entity.Area;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.entity.SeatMap;
import com.example.VieTicketSystem.model.entity.TableAdminStatistics;
import com.example.VieTicketSystem.model.entity.User;

@Repository
public class AdminRepo {
    @Autowired
    OrganizerRepo organizerRepo = new OrganizerRepo();

    public ArrayList<Organizer> viewAllListAprrove() throws ClassNotFoundException, SQLException {
        ArrayList<Organizer> organizersList = new ArrayList<>();
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
        rs.close();
        ps.close();
        con.close();
        return organizersList;
    }

    public void approveOrganizers(int organizer_id)
            throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement(
                "UPDATE Organizer SET is_active = 1 WHERE organizer_id = ?");
        ps.setInt(1, organizer_id);

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public ArrayList<Event> viewAllListApproveEvent() throws Exception {
        ArrayList<Event> eventList = new ArrayList<>();

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

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement(
                "UPDATE Event SET is_approve = 1 WHERE event_id = ?");
        ps.setInt(1, eventId);

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void rejectEvents(int eventId)
            throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement(
                "UPDATE Event SET is_approve = 3 WHERE event_id = ?");
        ps.setInt(1, eventId);

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void deleteEventById(int eventId) throws Exception {

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

    public AdminStatistics getStatisticsForAdmin() throws Exception {
        Class.forName(Baseconnection.nameClass);
        AdminStatistics stats = new AdminStatistics();

        String sql = "SELECT "
                + "(SELECT SUM(s.ticket_price) "
                + " FROM Ticket t "
                + " JOIN Seat s ON t.seat_id = s.seat_id "
                + " JOIN Area a ON s.row_id = a.area_id "
                + " JOIN Event e ON a.event_id = e.event_id "
                + " WHERE t.status = 0 AND e.end_date > NOW()) AS total_sold_amount_active_events, "
                + "(SELECT COUNT(*) "
                + " FROM Event "
                + " WHERE end_date > NOW()) AS total_ongoing_events, "
                + "(SELECT COUNT(*) "
                + " FROM Event "
                + " WHERE is_approve = 0) AS total_approved_events, "
                + "(SELECT COUNT(*) "
                + " FROM Event "
                + " WHERE is_approve = 3) AS total_rejected_events, "
                + "(SELECT COUNT(*) "
                + " FROM Organizer "
                + " WHERE is_active = 0) AS total_inactive_organizers, "
                + "(SELECT COUNT(*) "
                + " FROM Organizer "
                + " WHERE is_active = 1) AS total_active_organizers, "
                + "(SELECT COUNT(*) "
                + " FROM User u "
                + " WHERE u.role NOT IN ('a', 'b','p')) AS total_users_excluding_organizers, "
                + "(SELECT COUNT(*) "
                + " FROM User "
                + " WHERE role = 'b' OR role ='p') AS total_banned_users";

        try (Connection con = ConnectionPoolManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.setTotalSoldAmountActiveEvents(rs.getDouble("total_sold_amount_active_events"));
                stats.setTotalOngoingEvents(rs.getInt("total_ongoing_events"));
                stats.setTotalApprovedEvents(rs.getInt("total_approved_events"));
                stats.setTotalRejectedEvents(rs.getInt("total_rejected_events"));
                stats.setTotalInactiveOrganizers(rs.getInt("total_inactive_organizers"));
                stats.setTotalActiveOrganizers(rs.getInt("total_active_organizers"));
                stats.setTotalUsersExcludingOrganizers(rs.getInt("total_users_excluding_organizers"));
                stats.setTotalBannedUsers(rs.getInt("total_banned_users")); // Thêm dòng này
            }

        }

        return stats;
    }

    public List<Integer> getDailyRevenue() throws Exception {
        List<Integer> dailyRevenue = new ArrayList<>();
        Class.forName(Baseconnection.nameClass);
        try (Connection con = ConnectionPoolManager.getConnection()) {
            String sql = "SELECT DATE(t.purchase_date) AS `day`, SUM(s.ticket_price) AS daily_revenue " +
                    "FROM Ticket t " +
                    "JOIN Seat s ON t.seat_id = s.seat_id " +
                    "WHERE t.status = 0 " +
                    "GROUP BY DATE(t.purchase_date) " +
                    "ORDER BY DATE(t.purchase_date);";
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dailyRevenue.add(rs.getInt("daily_revenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Error retrieving daily revenue", e);
        }
        return dailyRevenue;
    }

    public List<Integer> getMonthlyRevenue() throws Exception {
        List<Integer> monthlyRevenue = new ArrayList<>();
        Class.forName(Baseconnection.nameClass);
        try (Connection con = ConnectionPoolManager.getConnection()) {
            String sql = "SELECT DATE_FORMAT(t.purchase_date, '%Y-%m') AS `month`, SUM(s.ticket_price) AS monthly_revenue "
                    +
                    "FROM Ticket t " +
                    "JOIN Seat s ON t.seat_id = s.seat_id " +
                    "WHERE t.status = 0 " +
                    "GROUP BY DATE_FORMAT(t.purchase_date, '%Y-%m') " +
                    "ORDER BY DATE_FORMAT(t.purchase_date, '%Y-%m');";
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    monthlyRevenue.add(rs.getInt("monthly_revenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Error retrieving monthly revenue", e);
        }
        return monthlyRevenue;
    }

    public List<TableAdminStatistics> getEventRevenues() throws Exception {
        List<TableAdminStatistics> eventRevenues = new ArrayList<>();
        String sql = "SELECT e.name AS eventName, u.full_name AS organizerName, SUM(a.ticket_price) AS revenue "
                +
                "FROM Event e " +
                "JOIN Organizer o ON e.organizer_id = o.organizer_id " +
                "JOIN User u ON o.organizer_id = u.user_id " +
                "JOIN Area a ON e.event_id = a.event_id " +
                "JOIN Seat s ON a.area_id = s.row_id " +
                "JOIN Ticket t ON s.seat_id = t.seat_id " +
                "JOIN `Order` r ON t.order_id = r.order_id " +
                "WHERE t.status = FALSE " +
                "GROUP BY e.event_id, u.full_name";

        Class.forName(Baseconnection.nameClass);
        try (Connection con = ConnectionPoolManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String eventName = rs.getString("eventName");
                String organizerName = rs.getString("organizerName");
                double revenue = rs.getDouble("revenue");
                eventRevenues.add(new TableAdminStatistics(eventName, organizerName, revenue));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Error retrieving event revenues", e);
        }

        return eventRevenues;
    }

    public ArrayList<User> getAllUser() throws Exception {
        ArrayList<User> users = new ArrayList<>();
        Connection con = ConnectionPoolManager.getConnection();
        String query = "SELECT * FROM User WHERE role != 'a' AND  role != 'b' AND  role != 'p'";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setDob(rs.getDate("dob"));
            user.setGender(rs.getString("gender").charAt(0));
            user.setAvatar(rs.getString("avatar"));
            user.setRole(rs.getString("role").charAt(0));
            user.setEmail(rs.getString("email"));
            users.add(user);
        }

        rs.close();
        ps.close();
        con.close();

        return users;
    }

    public ArrayList<User> getAllBanner() throws Exception {
        ArrayList<User> users = new ArrayList<>();
        Connection con = ConnectionPoolManager.getConnection();
        String query = "SELECT * FROM User WHERE role = 'b' OR role = 'p'";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFullName(rs.getString("full_name"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setDob(rs.getDate("dob"));
            user.setGender(rs.getString("gender").charAt(0));
            user.setAvatar(rs.getString("avatar"));
            user.setRole(rs.getString("role").charAt(0));
            user.setEmail(rs.getString("email"));
            users.add(user);
        }

        rs.close();
        ps.close();
        con.close();

        return users;
    }

    public void lockUser(int userId, char currentRole) throws Exception {
        Connection con = ConnectionPoolManager.getConnection();
        String query;
        if (currentRole == 'u') {
            query = "UPDATE User SET role = 'b' WHERE user_id = ?";
        } else if (currentRole == 'o') {
            query = "UPDATE User SET role = 'p' WHERE user_id = ?";
        } else {
            throw new IllegalArgumentException("Invalid role: " + currentRole);
        }

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        int rowsUpdated = ps.executeUpdate();

        if (rowsUpdated > 0) {
            System.out.println("User with ID " + userId + " has been locked.");
        } else {
            System.out.println("No user with ID " + userId + " found.");
        }

        ps.close();
        con.close();
    }

    public void unlockUser(int userId, char currentRole) throws Exception {
        Connection con = ConnectionPoolManager.getConnection();
        String query;
        if (currentRole == 'b') {
            query = "UPDATE User SET role = 'u' WHERE user_id = ?";
        } else if (currentRole == 'p') {
            query = "UPDATE User SET role = 'o' WHERE user_id = ?";
        } else {
            throw new IllegalArgumentException("Invalid role: " + currentRole);
        }

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        int rowsUpdated = ps.executeUpdate();

        if (rowsUpdated > 0) {
            System.out.println("User with ID " + userId + " has been unlocked.");
        } else {
            System.out.println("No user with ID " + userId + " found.");
        }

        ps.close();
        con.close();
    }

}
