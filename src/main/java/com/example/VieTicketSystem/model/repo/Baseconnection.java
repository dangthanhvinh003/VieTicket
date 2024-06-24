package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Baseconnection {
    static String url = "jdbc:mysql://localhost:3306/VieTicket1";
    static String username = "root";
    static String password = "khanhtran0905";
    static String nameClass = "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(nameClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Driver not found");
        }
        return DriverManager.getConnection(url, username, password);
    }
}
