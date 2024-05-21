package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.UnverifiedUser;

@Repository
public class UnverifiedUserRepo {

    private static final String INSERT_STATEMENT = "INSERT INTO UnverifiedUsers (userid, created_at) VALUES (?, ?)";
    private static final String SELECT_STATEMENT = "SELECT * FROM UnverifiedUsers WHERE userid = ?";
    private static final String DELETE_STATEMENT = "DELETE FROM UnverifiedUsers WHERE userid = ?";

    public void saveNew(UnverifiedUser unverifiedUser) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(INSERT_STATEMENT)) {

            ps.setInt(1, unverifiedUser.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(unverifiedUser.getCreatedAt()));

            ps.executeUpdate();
        }
    }

    public boolean isUnverified(int userId) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(SELECT_STATEMENT)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Check if a matching record exists
            }
        }
    }

    public UnverifiedUser findById(int userId) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(SELECT_STATEMENT)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                UnverifiedUser unverifiedUser = null;
                if (rs.next()) {
                    unverifiedUser = new UnverifiedUser();
                    unverifiedUser.setUserId(rs.getInt("userid"));
                    unverifiedUser.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                return unverifiedUser;
            }
        }
    }

    public void deleteById(int userId) throws Exception {
        Class.forName(Baseconnection.nameClass);
        try (Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(DELETE_STATEMENT)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
