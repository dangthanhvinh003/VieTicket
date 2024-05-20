package com.example.VieTicketSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:secrets.properties")
public class VieTicketSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(VieTicketSystemApplication.class, args);
	}
}
