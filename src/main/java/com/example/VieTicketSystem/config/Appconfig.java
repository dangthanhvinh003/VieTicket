package com.example.VieTicketSystem.config;

import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.EventRepoImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Appconfig {

    @Bean
    public EventRepo eventRepo() {
        return new EventRepoImpl();
    }
}