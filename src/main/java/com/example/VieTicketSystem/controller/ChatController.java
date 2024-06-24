package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.entity.ChatMessage;
import com.example.VieTicketSystem.model.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        System.out.println(chatMessage.getContent());
        chatMessage.setTimestamp(LocalDateTime.now());
        chatService.sendMessage(chatMessage.getSender(), chatMessage.getReceiver(), chatMessage.getContent());
        return chatMessage;
    }

    @MessageMapping("/chat.getMessages")
    public void getMessages(String sender, String receiver) {
        messagingTemplate.convertAndSend("/topic/messages", chatService.getMessages(sender, receiver));
    }

    @GetMapping("/chat/allMessages")
    @ResponseBody
    public List<ChatMessage> getAllMessages() {
        return chatService.getAllMessages();
    }
}