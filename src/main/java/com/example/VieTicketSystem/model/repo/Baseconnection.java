package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Baseconnection {
    public static String url = "jdbc:mysql://localhost:3306/VieTicket1";
    public static String username = "root";
    public static String password = "thanhvinh";
    public static String nameClass = "com.mysql.cj.jdbc.Driver";
    
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
