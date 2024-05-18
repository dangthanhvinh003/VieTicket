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
	@Autowired
	private EmailService emailService;

	public static void main(String[] args) {
		SpringApplication.run(VieTicketSystemApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void triggerMail() {
		try {
			emailService.sendEmail("jsaluluftdl@gmail.com", "Test", "Hi mom");
			System.out.println("Mail sent successfully");
		} catch (MessagingException e) {
			System.out.println("Failed to send mail");
			e.printStackTrace();
		}
	}

}
