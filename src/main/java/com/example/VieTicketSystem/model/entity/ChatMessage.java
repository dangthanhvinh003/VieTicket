package com.example.VieTicketSystem.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;


@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class ChatMessage {
    private int id;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
}