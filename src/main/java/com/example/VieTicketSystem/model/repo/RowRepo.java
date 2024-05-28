package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Row;

@Repository
public class RowRepo {
    public void addRow(String rowName, int AreaId)
            throws ClassNotFoundException, SQLException {

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO `ROW` (row_name, area_id) VALUES (?, ?)");

        ps.setString(1, rowName);
        ps.setInt(2, AreaId);
        ps.executeUpdate();
        ps.close();
    }

    public int getIdRowByAreaId(int area) throws ClassNotFoundException, SQLException {
        int rowId = -1; // Giá trị mặc định khi không tìm thấy hàng

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = connection.prepareStatement(
                "SELECT row_id FROM `ROW` WHERE area_id = ?");
        ps.setInt(1, area);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            rowId = rs.getInt("row_id");
        }

        rs.close();
        ps.close();
        connection.close();

        return rowId;
    }
    public ArrayList<Integer> getAllRowIdsByAreaId(int areaId) throws ClassNotFoundException, SQLException {
        ArrayList<Integer> rowIds = new ArrayList<>();

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
        
        String sql = "SELECT row_id FROM `ROW` WHERE area_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, areaId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            rowIds.add(rs.getInt("row_id"));
        }

        rs.close();
        ps.close();
        connection.close();

        return rowIds;
    }
     // Phương thức lấy tất cả các hàng theo area_id
    public ArrayList<Row> getAllRowsByAreaId(int areaId) throws ClassNotFoundException, SQLException {
        ArrayList<Row> rows = new ArrayList<>();

        Class.forName(Baseconnection.nameClass);
        Connection connection = DriverManager.getConnection(Baseconnection.url, Baseconnection.username, Baseconnection.password);
        
        String sql = "SELECT row_id, row_name, area_id FROM `ROW` WHERE area_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, areaId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int rowId = rs.getInt("row_id");
            String rowName = rs.getString("row_name");
            int areaIdFromDb = rs.getInt("area_id");
            Row row = new Row(rowName, rowId, areaIdFromDb);
            rows.add(row);
        }

        rs.close();
        ps.close();
        connection.close();

        return rows;
    }
}
