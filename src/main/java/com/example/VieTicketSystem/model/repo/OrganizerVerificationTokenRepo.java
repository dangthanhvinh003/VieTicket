package com.example.VieTicketSystem.model.repo;

import com.example.VieTicketSystem.model.entity.OrganizerVerificationToken;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;

@Repository
public class OrganizerVerificationTokenRepo {

    private static final String INSERT_TOKEN = "INSERT INTO OrganizerVerificationToken (organizer_id, token, familiar_name, expiry_date, created_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_TOKEN = "SELECT * FROM OrganizerVerificationToken WHERE token = ?";
    private static final String DELETE_TOKEN = "DELETE FROM OrganizerVerificationToken WHERE token = ?";
    private static final String DELETE_EXPIRED_TOKENS = "DELETE FROM OrganizerVerificationToken WHERE expiry_date < ?";
    private final OrganizerRepo organizerRepo;

    public OrganizerVerificationTokenRepo(OrganizerRepo organizerRepo) {
        this.organizerRepo = organizerRepo;
    }

    public void insertToken(OrganizerVerificationToken token) throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TOKEN)) {
            preparedStatement.setInt(1, token.getOrganizer().getUserId());
            preparedStatement.setString(2, token.getToken());
            preparedStatement.setString(3, token.getFamiliarName());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(token.getExpiryDate()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(token.getCreatedAt()));
            preparedStatement.executeUpdate();

        }
    }

    public OrganizerVerificationToken getToken(String token) throws Exception {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TOKEN)) {
            preparedStatement.setString(1, token);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    OrganizerVerificationToken organizerVerificationToken = new OrganizerVerificationToken();
                    organizerVerificationToken.setOrganizer(organizerRepo.findById(resultSet.getInt("organizer_id")));
                    organizerVerificationToken.setToken(resultSet.getString("token"));
                    organizerVerificationToken.setFamiliarName(resultSet.getString("familiar_name"));
                    organizerVerificationToken.setExpiryDate(resultSet.getTimestamp("expiry_date").toLocalDateTime());
                    organizerVerificationToken.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    return organizerVerificationToken;
                }
            }
        }
        return null;
    }

    public void deleteToken(String token) throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_TOKEN)) {
            preparedStatement.setString(1, token);
            preparedStatement.executeUpdate();
        }
    }

    public void deleteExpiredTokens() throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_EXPIRED_TOKENS)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.executeUpdate();
        }
    }
}
