package com.example.VieTicketSystem.repo;

import java.sql.*;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Organizer;

@Repository
public class OrganizerRepo {

    private static final String INSERT_STATEMENT = "CALL InsertOrganizer(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_STATEMENT = "CALL UpdateOrganizer(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_STATEMENT = "SELECT * FROM OrganizerDetails WHERE organizer_id = ?";

    public void saveNew(Organizer organizer) throws Exception {

        try (Connection con = ConnectionPoolManager.getConnection()) {
            CallableStatement cs = con
                    .prepareCall("{call InsertOrganizer(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, organizer.getFullName());
            cs.setString(2, organizer.getUsername());
            cs.setString(3, organizer.getPassword());
            cs.setString(4, organizer.getPhone());
            cs.setDate(5, new Date(organizer.getDob().getTime())); // Convert to SQL Date
            cs.setString(6, String.valueOf(organizer.getGender()));
            cs.setString(7, organizer.getAvatar());
            cs.setString(8, String.valueOf(organizer.getRole()));
            cs.setString(9, organizer.getEmail());
            cs.setDate(10, new Date(organizer.getFoundedDate().getTime())); // Convert to SQL Date
            cs.setString(11, organizer.getWebsite());
            cs.setBoolean(12, organizer.isActive());
            cs.setString(13, organizer.getOrganizerAddr());
            cs.setString(14, organizer.getOrganizerType());
            cs.registerOutParameter(15, Types.INTEGER);
            cs.executeUpdate();

            int userId = cs.getInt(15);
            organizer.setUserId(userId);
        }
    }

    public void save(Organizer organizer) throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement(UPDATE_STATEMENT);
        ps.setInt(1, organizer.getUserId());
        ps.setString(2, organizer.getFullName());
        ps.setString(3, organizer.getUsername());
        ps.setString(4, organizer.getPassword());
        ps.setString(5, organizer.getPhone());
        ps.setDate(6, new Date(organizer.getDob().getTime())); // Convert to SQL Date
        ps.setString(7, String.valueOf(organizer.getGender()));
        ps.setString(8, organizer.getAvatar());
        ps.setString(9, String.valueOf(organizer.getRole()));
        ps.setString(10, organizer.getEmail());
        ps.setDate(11, new Date(organizer.getFoundedDate().getTime())); // Convert to SQL Date
        ps.setString(12, organizer.getWebsite());
        ps.setBoolean(13, organizer.isActive());
        ps.setString(14, organizer.getOrganizerAddr());
        ps.setString(15, organizer.getOrganizerType());

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public Organizer findById(int id) throws Exception {

        Connection con = ConnectionPoolManager.getConnection();
        PreparedStatement ps = con.prepareStatement(SELECT_STATEMENT);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        Organizer organizer = null;
        if (rs.next()) {
            organizer = new Organizer();
            organizer.setUserId(rs.getInt("organizer_id"));
            organizer.setFullName(rs.getString("full_name"));
            organizer.setUsername(rs.getString("username"));
            organizer.setPassword(rs.getString("password"));
            organizer.setPhone(rs.getString("phone"));
            organizer.setDob(rs.getDate("dob"));
            organizer.setGender(rs.getString("gender").charAt(0));
            organizer.setAvatar(rs.getString("avatar"));
            organizer.setRole(rs.getString("role").charAt(0));
            organizer.setEmail(rs.getString("email"));
            organizer.setFoundedDate(rs.getDate("founded_date"));
            organizer.setWebsite(rs.getString("website"));
            organizer.setActive(rs.getBoolean("is_active"));
            organizer.setOrganizerAddr(rs.getString("organizer_addr"));
            organizer.setOrganizerType(rs.getString("organizer_type"));
        }

        rs.close();
        ps.close();
        con.close();
        return organizer;
    }

    public Organizer getOrganizerByUserId(int userId) throws SQLException {
        Organizer organizer = null;
        String sql = "SELECT * FROM `Organizer` WHERE organizer_id = ?";

        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    organizer = new Organizer();

                    organizer.setFoundedDate(resultSet.getDate("founded_date"));
                    organizer.setWebsite(resultSet.getString("website"));
                    organizer.setActive(resultSet.getBoolean("is_active"));
                    organizer.setOrganizerAddr(resultSet.getString("organizer_addr"));
                    organizer.setOrganizerType(resultSet.getString("organizer_type"));
                }
            }
        }

        return organizer;
    }

    public Organizer getOrganizerByEventId(int eventId) throws SQLException {
        Organizer organizer = null;
        String sql = "SELECT "
                + "User.user_id, User.full_name, User.username, User.password, User.phone, User.dob, User.gender, User.avatar, User.role, User.email, "
                + "Organizer.founded_date, Organizer.website, Organizer.organizer_addr, Organizer.organizer_type, Organizer.is_active "
                + "FROM Organizer "
                + "JOIN User ON Organizer.organizer_id = User.user_id "
                + "JOIN Event ON Organizer.organizer_id = Event.organizer_id "
                + "WHERE Event.event_id = ?;";


        try (Connection con = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, eventId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    organizer = new Organizer();

                    // Set User attributes
                    organizer.setUserId(resultSet.getInt("user_id"));
                    organizer.setFullName(resultSet.getString("full_name"));
                    organizer.setUsername(resultSet.getString("username"));
                    organizer.setPassword(resultSet.getString("password"));
                    organizer.setPhone(resultSet.getString("phone"));
                    organizer.setDob(resultSet.getDate("dob"));
                    organizer.setGender(resultSet.getString("gender").charAt(0));
                    organizer.setAvatar(resultSet.getString("avatar"));
                    organizer.setRole(resultSet.getString("role").charAt(0));
                    organizer.setEmail(resultSet.getString("email"));

                    // Set Organizer-specific attributes
                    organizer.setFoundedDate(resultSet.getDate("founded_date"));
                    organizer.setWebsite(resultSet.getString("website"));
                    organizer.setOrganizerAddr(resultSet.getString("organizer_addr"));
                    organizer.setOrganizerType(resultSet.getString("organizer_type"));
                    organizer.setActive(resultSet.getBoolean("is_active"));
                }
            }
        }

        return organizer;
    }

    public Double getAverageRatingForOrganizer(int organizerId) throws SQLException {
        try (Connection conn = ConnectionPoolManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT AVG(star) FROM Rating WHERE organizer_id = ?");
        ) {
            stmt.setInt(1, organizerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1); // Assuming the result is a double (AVG function result)
                }
            }
        }

        return null; // Return null if no rating is found for the organizer
    }


}