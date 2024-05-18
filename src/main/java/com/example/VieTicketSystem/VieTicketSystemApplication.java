package com.example.VieTicketSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;

import com.example.VieTicketSystem.model.service.EmailService;

import jakarta.mail.MessagingException;

@SpringBootApplication
@PropertySource("classpath:secrets.properties")
public class VieTicketSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(VieTicketSystemApplication.class, args);
	}
}
