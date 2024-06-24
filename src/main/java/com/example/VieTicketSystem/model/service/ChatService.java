package com.example.VieTicketSystem.model.service;

import com.example.VieTicketSystem.model.entity.ChatMessage;
import com.example.VieTicketSystem.model.repo.ChatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatRepo chatRepo;

    @Autowired
    public ChatService(ChatRepo chatRepo) {
        this.chatRepo = chatRepo;
    }

    public void sendMessage(String sender, String receiver, String content) {
        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        chatRepo.saveMessage(message);
    }

    public List<ChatMessage> getMessages(String sender, String receiver) {
        return chatRepo.getMessages(sender, receiver);
    }

    public List<ChatMessage> getAllMessages() {
        return chatRepo.getAllMessages();
    }
}