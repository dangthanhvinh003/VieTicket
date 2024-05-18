package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.User;
@Repository
public class UserRepo {
    public void EditImgUser(String img, int id) throws Exception {

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(
                "UPDATE User SET avatar = ? where user_id = ?");
        ps.setString(1, img);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();

    }
}
