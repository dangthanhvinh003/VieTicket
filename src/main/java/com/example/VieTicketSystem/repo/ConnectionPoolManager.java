package com.example.VieTicketSystem.repo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class ConnectionPoolManager {
    private static final HikariConfig config = new HikariConfig();
    @Getter
    private static HikariDataSource dataSource;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void initializeDataSource() {
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(32);
        dataSource = new HikariDataSource(config);
    }

    private ConnectionPoolManager() {
    }

    @Bean
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
