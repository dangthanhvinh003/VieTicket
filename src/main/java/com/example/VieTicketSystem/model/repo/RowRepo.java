package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Row;

@Repository
public class RowRepo {
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM `Row` WHERE row_id = ?";
    private static final String SELECT_BY_AREA_ID_SQL = "SELECT * FROM `Row` WHERE area_id = ?";
    private final AreaRepo areaRepo;

    public RowRepo(AreaRepo areaRepo) {
        this.areaRepo = areaRepo;
    }

    public Row findById(int id) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_SQL);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Row row = null;
        if (rs.next()) {
            row = new Row();
            row.setRowId(rs.getInt("row_id"));
            row.setRowName(rs.getString("row_name"));
            row.setArea(areaRepo.findById(rs.getInt("area_id")));
        }
        rs.close();
        ps.close();
        connection.close();
        return row;
    }

    public List<Row> findByAreaId(int areaId) throws Exception {
        Class.forName(Baseconnection.nameClass);
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(SELECT_BY_AREA_ID_SQL);
        ps.setInt(1, areaId);
        ResultSet rs = ps.executeQuery();
        List<Row> rows = new ArrayList<>();
        while (rs.next()) {
            Row row = new Row();
            row.setRowId(rs.getInt("row_id"));
            row.setRowName(rs.getString("row_name"));
            row.setArea(areaRepo.findById(rs.getInt("area_id")));
            rows.add(row);
        }
        rs.close();
        ps.close();
        connection.close();
        return rows;
    }

    public void addRow(String rowName, int AreaId)
            throws ClassNotFoundException, SQLException {

        Class.forName(Baseconnection.nameClass);
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO `Row` (row_name, area_id) VALUES (?, ?)");

        ps.setString(1, rowName);
        ps.setInt(2, AreaId);
        ps.executeUpdate();
        ps.close();
    }

    public int getIdRowByAreaId(int area) throws ClassNotFoundException, SQLException {
        int rowId = -1; // Giá trị mặc định khi không tìm thấy hàng

        Class.forName(Baseconnection.nameClass);
        Connection connection = ConnectionPoolManager.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT row_id FROM `Row` WHERE area_id = ?");
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
        Connection connection = ConnectionPoolManager.getConnection();
        
        String sql = "SELECT row_id FROM `Row` WHERE area_id = ?";
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
    public ArrayList<Row> getAllRowsByAreaId(int areaId) throws Exception {
        ArrayList<Row> rows = new ArrayList<>();

        Class.forName(Baseconnection.nameClass);
        Connection connection = ConnectionPoolManager.getConnection();
        
        String sql = "SELECT row_id, row_name, area_id FROM `Row` WHERE area_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, areaId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int rowId = rs.getInt("row_id");
            String rowName = rs.getString("row_name");
            int areaIdFromDb = rs.getInt("area_id");
            Row row = new Row(rowName, rowId, areaRepo.findById(areaIdFromDb));
            rows.add(row);
        }

        rs.close();
        ps.close();
        connection.close();

        return rows;
    }
}
