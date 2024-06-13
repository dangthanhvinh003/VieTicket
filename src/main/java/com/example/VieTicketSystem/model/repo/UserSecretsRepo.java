package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;

@Repository
public class UserSecretsRepo {

    private static final String INSERT_SECRET_KEY_SQL = "INSERT INTO UserSecrets(user_id, secret_key) VALUES (?, ?)";
    private static final String SELECT_SECRET_KEY_SQL = "SELECT secret_key FROM UserSecrets WHERE user_id = ?";
    private static final String DELETE_SECRET_KEY_SQL = "DELETE FROM UserSecrets WHERE user_id = ?";

    public void insertSecretKey(int userId, String secretKey) throws SQLException {
        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SECRET_KEY_SQL)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, secretKey);

            preparedStatement.executeUpdate();
        }
    }

    public String getSecretKey(int userId) throws SQLException {
        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SECRET_KEY_SQL)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("secret_key");
                }
            }
        }

        return null;
    }

    public void deleteSecretKey(int userId) throws SQLException {
        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SECRET_KEY_SQL)) {

            preparedStatement.setInt(1, userId);

            preparedStatement.executeUpdate();
        }
    }

    public void rotateSecretKey(int userId, String secretKey) throws SQLException {
        deleteSecretKey(userId);
        insertSecretKey(userId, secretKey);
    }
}