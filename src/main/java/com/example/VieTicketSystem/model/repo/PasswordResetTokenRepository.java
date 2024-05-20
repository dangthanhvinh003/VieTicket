package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.PasswordResetToken;

@Repository
public class PasswordResetTokenRepository {
    private static final String INSERT_TOKEN_SQL = "INSERT INTO PasswordResetToken(user_id, token, created_at, expiry_date) VALUES (?, ?, ?, ?)";
    private static final String SELECT_TOKEN_SQL = "SELECT token FROM PasswordResetToken WHERE user_id = ?";
    private static final String GET_TOKEN_AS_OBJECT_SQL = "SELECT * FROM PasswordResetToken WHERE user_id = ?";
    private static final String FIND_BY_TOKEN_SQL = "SELECT * FROM PasswordResetToken WHERE token = ?";
    private static final String DELETE_TOKEN_SQL = "DELETE FROM PasswordResetToken WHERE user_id = ?";
    private static final String CLEAN_UP_EXPIRED_TOKENS_SQL = "DELETE FROM PasswordResetToken WHERE expiry_date < NOW()";

    public void insertToken(PasswordResetToken token) throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TOKEN_SQL)) {

            preparedStatement.setInt(1, token.getUserId());
            preparedStatement.setString(2, token.getToken());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(token.getCreatedAt()));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(token.getExpiryDate()));

            preparedStatement.executeUpdate();
        }
    }

    public String getTokenString(int userId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TOKEN_SQL)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("token");
                }
            }
        }

        return null;
    }

    public PasswordResetToken getToken(int userId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement preparedStatement = connection.prepareStatement(GET_TOKEN_AS_OBJECT_SQL)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    PasswordResetToken passwordResetToken = new PasswordResetToken();
                    passwordResetToken.setUserId(resultSet.getInt("user_id"));
                    passwordResetToken.setToken(resultSet.getString("token"));
                    passwordResetToken.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    passwordResetToken.setExpiryDate(resultSet.getTimestamp("expiry_date").toLocalDateTime());
                    return passwordResetToken;
                }
            }
        }

        return null;
    }

    public void deleteToken(int userId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement preparedStatement = connection.prepareStatement(DELETE_TOKEN_SQL)) {

            preparedStatement.setInt(1, userId);

            preparedStatement.executeUpdate();
        }
    }

    public PasswordResetToken findByToken(String token) throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_TOKEN_SQL)) {

            preparedStatement.setString(1, token);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    PasswordResetToken passwordResetToken = new PasswordResetToken();
                    passwordResetToken.setUserId(resultSet.getInt("user_id"));
                    passwordResetToken.setToken(resultSet.getString("token"));
                    passwordResetToken.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    passwordResetToken.setExpiryDate(resultSet.getTimestamp("expiry_date").toLocalDateTime());
                    return passwordResetToken;
                }
            }
        }

        return null;
    }

    public void cleanUpExpiredTokens() throws SQLException {
        try (Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
                PreparedStatement preparedStatement = connection.prepareStatement(CLEAN_UP_EXPIRED_TOKENS_SQL)) {

            preparedStatement.executeUpdate();
        }
    }
}
