package com.example.VieTicketSystem.model.repo;

import com.example.VieTicketSystem.model.entity.ChatMessage;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ChatRepo {

    public void saveMessage(ChatMessage message) {
        String sql = "INSERT INTO chat_messages (sender, receiver, content, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection connection = Baseconnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, message.getSender());
            preparedStatement.setString(2, message.getReceiver());
            preparedStatement.setString(3, message.getContent());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(message.getTimestamp()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ChatMessage> getMessages(String sender, String receiver) {
        String sql = "SELECT * FROM chat_messages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY timestamp";
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection connection = Baseconnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, sender);
            preparedStatement.setString(2, receiver);
            preparedStatement.setString(3, receiver);
            preparedStatement.setString(4, sender);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ChatMessage message = new ChatMessage();
                message.setId(resultSet.getInt("id"));
                message.setSender(resultSet.getString("sender"));
                message.setReceiver(resultSet.getString("receiver"));
                message.setContent(resultSet.getString("content"));
                message.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<ChatMessage> getAllMessages() {
        String sql = "SELECT * FROM chat_messages ORDER BY timestamp";
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection connection = Baseconnection.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                ChatMessage message = new ChatMessage();
                message.setId(resultSet.getInt("id"));
                message.setSender(resultSet.getString("sender"));
                message.setReceiver(resultSet.getString("receiver"));
                message.setContent(resultSet.getString("content"));
                message.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}