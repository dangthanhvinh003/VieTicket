package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.entity.User;

@Repository
public class OrganizerRepo {

    private static final String INSERT_STATEMENT = "INSERT INTO Organizer (organizer_id, founded_date, website, is_active, organizer_addr, organizer_type) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_STATEMENT = "UPDATE Organizer SET founded_date = ?, website = ?, is_active = ?, organizer_addr = ?, organizer_type = ? WHERE organizer_id = ?";

    @Autowired
    private UserRepo userRepo;

    public void saveNew(Organizer organizer) throws Exception {

        userRepo.saveNew(new User(organizer.getFullName(), organizer.getPhone(), organizer.getDob(),
                organizer.getGender(), organizer.getEmail(), organizer.getUsername(), organizer.getPassword(),
                organizer.getRole()));

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(INSERT_STATEMENT);
        ps.setInt(1, userRepo.findByUsername(organizer.getUsername()).getUserId()); // Associate the organizer with the newly saved user
        ps.setDate(2, new Date(organizer.getFoundedDate().getTime())); // Convert to SQL Date
        ps.setString(3, organizer.getWebsite());
        ps.setBoolean(4, organizer.isActive());
        ps.setString(5, organizer.getOrganizerAddr());
        ps.setString(6, organizer.getOrganizerType());

        ps.executeUpdate();
        ps.close();
        con.close();
    }

    public void save(Organizer organizer) throws Exception {
        userRepo.save(new User(organizer.getFullName(), organizer.getPhone(), organizer.getDob(),
                organizer.getGender(), organizer.getEmail(), organizer.getUsername(), organizer.getPassword(),
                organizer.getRole()));

        Class.forName(Baseconnection.nameClass);
        Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                Baseconnection.password);
        PreparedStatement ps = con.prepareStatement(UPDATE_STATEMENT);
        ps.setDate(1, new Date(organizer.getFoundedDate().getTime())); // Convert to SQL Date
        ps.setString(2, organizer.getWebsite());
        ps.setBoolean(3, organizer.isActive());
        ps.setString(4, organizer.getOrganizerAddr());
        ps.setString(5, organizer.getOrganizerType());
        ps.setInt(6, userRepo.findByUsername(organizer.getUsername()).getUserId()); // Associate the organizer with the newly saved user

        ps.executeUpdate();
        ps.close();
        con.close();
    }

}
