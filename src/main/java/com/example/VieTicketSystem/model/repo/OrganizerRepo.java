package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Organizer;

/*
 * View comments on the bottom of this source file
 */

@Repository
public class OrganizerRepo {

    private static final String INSERT_STATEMENT = "CALL InsertOrganizer(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_STATEMENT = "CALL UpdateOrganizer(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_STATEMENT = "SELECT * FROM OrganizerDetails WHERE organizer_id = ?";

    public void saveNew(Organizer organizer) throws Exception {

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(INSERT_STATEMENT);
        ps.setString(1, organizer.getFullName());
        ps.setString(2, organizer.getUsername());
        ps.setString(3, organizer.getPassword());
        ps.setString(4, organizer.getPhone());
        ps.setDate(5, new Date(organizer.getDob().getTime())); // Convert to SQL Date
        ps.setString(6, String.valueOf(organizer.getGender()));
        ps.setString(7, organizer.getAvatar());
        ps.setString(8, String.valueOf(organizer.getRole()));
        ps.setString(9, organizer.getEmail());
        ps.setDate(10, new Date(organizer.getFoundedDate().getTime())); // Convert to SQL Date
        ps.setString(11, organizer.getWebsite());
        ps.setBoolean(12, organizer.isActive());
        ps.setString(13, organizer.getOrganizerAddr());
        ps.setString(14, organizer.getOrganizerType());

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void save(Organizer organizer) throws Exception {

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
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

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
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
}