package com.example.VieTicketSystem.model.repo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class ConnectionPoolManager {
    private static final HikariConfig config = new HikariConfig();
    @Getter
    private static final HikariDataSource dataSource;

    static {
        config.setJdbcUrl(Baseconnection.url);
        config.setUsername(Baseconnection.username);
        config.setPassword(Baseconnection.password);
        config.setMaximumPoolSize(32);
        dataSource = new HikariDataSource(config);
    }

    private ConnectionPoolManager() {}

    @Bean
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


}
