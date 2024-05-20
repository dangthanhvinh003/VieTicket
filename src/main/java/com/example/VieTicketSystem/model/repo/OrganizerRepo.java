package com.example.VieTicketSystem.model.repo;


import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.entity.User;

@Repository
public class OrganizerRepo {
    public void saveNew(Organizer organizer) throws Exception {
        if (existsByUsername(organizer.getUsername())) {
            throw new Exception("Username already exists");
        }
        if (existsByEmail(organizer.getEmail())) {
            throw new Exception("Email already exists");
        }
        if (existsByPhone(organizer.getPhone())) {
            throw new Exception("Phone number already exists");
        }

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO Organizer (full_name, username, password, phone, dob, gender, avatar, role, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, organizer.getFullName());
        ps.setString(2, organizer.getUsername());
        ps.setString(3, organizer.getPassword());
        ps.setString(4, organizer.getPhone());
        ps.setDate(5, organizer.getDob());
        ps.setString(6, String.valueOf(organizer.getGender()));
        ps.setString(7, organizer.getAvatar());
        ps.setString(8, String.valueOf(organizer.getRole()));
        ps.setString(9, organizer.getEmail());
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void save(Organizer organizer) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(
                "UPDATE Organizer SET full_name = ?, username = ?, password = ?, phone = ?, dob = ?, gender = ?, avatar = ?, role = ?, email = ? WHERE organizer_id = ?");
        ps.setString(1, organizer.getFullName());
        ps.setString(2, organizer.getUsername());
        ps.setString(3, organizer.getPassword());
        ps.setString(4, organizer.getPhone());
        ps.setDate(5, organizer.getDob());
        ps.setString(6, String.valueOf(organizer.getGender()));
        ps.setString(7, organizer.getAvatar());
        ps.setString(8, String.valueOf(organizer.getRole()));
        ps.setString(9, organizer.getEmail());
        ps.setInt(10, organizer.getUserId()); // Assuming that User has a getId() method that returns the user's ID
        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public boolean existsByUsername(String username) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM `User` WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }

    public boolean existsByEmail(String email) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM `User` WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }

    public boolean existsByPhone(String phone) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement("SELECT 1 FROM `User` WHERE phone = ?");
        ps.setString(1, phone);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        con.close();
        return exists;
    }

}
