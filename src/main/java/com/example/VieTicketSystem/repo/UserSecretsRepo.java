package com.example.VieTicketSystem.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import com.example.VieTicketSystem.model.dto.UserSecretInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Repository
public class UserSecretsRepo {

    private static final String INSERT_SECRET_KEY_SQL = "INSERT INTO UserSecrets(user_id, secret_key, created_at) VALUES (?, ?, ?)";
    private static final String SELECT_SECRET_KEY_SQL = "SELECT secret_key FROM UserSecrets WHERE user_id = ?";
    private static final String DELETE_SECRET_KEY_SQL = "DELETE FROM UserSecrets WHERE user_id = ?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM UserSecrets WHERE user_id = ?";
    @Value("${ENCRYPTION_KEY}")
    private String ENCRYPTION_KEY;

    public UserSecretInfo findByUserId(int userId) throws Exception {
        try (Connection connection = ConnectionPoolManager.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(FIND_BY_ID_SQL);

            psmt.setInt(1, userId);

            try (ResultSet rs = psmt.executeQuery()) {
                if (rs.next()) {
                    UserSecretInfo userSecretInfo = new UserSecretInfo();
                    userSecretInfo.setUserId(rs.getInt("user_id"));
                    userSecretInfo.setSecretKey(decrypt(rs.getString("secret_key")));
                    userSecretInfo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return userSecretInfo;
                }
            }
        }

        return null;
    }

    public void insertSecretKey(int userId, String secretKey) throws Exception {

        // Encrypt the secret key before storing it
        String encryptedSecretKey = encrypt(secretKey);
        System.out.println(encryptedSecretKey.length());

        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SECRET_KEY_SQL)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, encryptedSecretKey);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));

            preparedStatement.executeUpdate();
        }
    }

    public String getSecretKey(int userId) throws Exception {
        try (Connection connection = ConnectionPoolManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SECRET_KEY_SQL)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Decrypt the secret key before returning it
                    return decrypt(resultSet.getString("secret_key"));
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

    private String encrypt(String secretKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(secretKey.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encryptedSecretKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedSecretKey));
        return new String(decrypted);
    }
}