package com.example.VieTicketSystem.model.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Organizer;

@Repository
public class AdminRepo {
        @Autowired 
        OrganizerRepo organizerRepo = new OrganizerRepo();
        public ArrayList<Organizer> viewAllListAprrove() throws ClassNotFoundException, SQLException {
                ArrayList<Organizer> organizersList = new ArrayList<>();
                Class.forName(Baseconnection.nameClass);
                Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(
                                "SELECT " +
                                                "o.organizer_id, " +
                                                "u.full_name, " +
                                                "u.phone, " +
                                                "u.dob, " +
                                                "u.gender, " +
                                                "u.avatar, " +
                                                "u.role, " +
                                                "u.email, " +
                                                "o.founded_date, " +
                                                "o.website, " +
                                                "o.organizer_addr, " +
                                                "o.organizer_type " +
                                                "FROM " +
                                                "Organizer o " +
                                                "JOIN " +
                                                "User u ON o.organizer_id = u.user_id " +
                                                "WHERE " +
                                                "o.is_active = 0");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                        Organizer organizer = new Organizer();
                        organizer.setUserId(rs.getInt("organizer_id"));
                        organizer.setFullName(rs.getString("full_name"));
                        organizer.setPhone(rs.getString("phone"));
                        organizer.setDob(rs.getDate("dob"));
                        organizer.setGender(rs.getString("gender").charAt(0));
                        organizer.setAvatar(rs.getString("avatar"));
                        organizer.setRole(rs.getString("role").charAt(0));
                        organizer.setEmail(rs.getString("email"));
                        organizer.setFoundedDate(rs.getDate("founded_date"));
                        organizer.setWebsite(rs.getString("website"));
                        organizer.setOrganizerAddr(rs.getString("organizer_addr"));
                        organizer.setOrganizerType(rs.getString("organizer_type"));
                        organizersList.add(organizer);
                }
                return organizersList;

        }

        public void approveOrganizers(int organizer_id)
                        throws Exception {
                Class.forName(Baseconnection.nameClass);
                Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(
                                "UPDATE Organizer SET is_active = 1 WHERE organizer_id = ?");
                ps.setInt(1, organizer_id);

                ps.executeUpdate();
                ps.close();

        }

        public ArrayList<Event> viewAllListAprroveEvent() throws Exception {
                ArrayList<Event> eventList = new ArrayList<>();
                Class.forName(Baseconnection.nameClass);
                Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(
                               "SELECT * FROM EVENT WHERE is_approve = 0");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                        Event event = new Event();
                        event.setEventId(rs.getInt("event_id"));
                        event.setName(rs.getString("name"));
                        event.setDescription(rs.getString("description"));
                        event.setStartDate(rs.getDate("start_date"));
                        event.setLocation(rs.getString("location"));
                        event.setType(rs.getString("type"));
                        event.setTicketSaleDate(rs.getDate("ticket_sale_date"));
                        event.setEndDate(rs.getDate("end_date"));
                        event.setOrganizer(organizerRepo.findById(rs.getInt("organizer_id")));
                        event.setPoster(rs.getString("poster"));
                        event.setBanner(rs.getString("banner"));
                        event.setApprove(rs.getBoolean("is_approve"));
        
                        eventList.add(event);
                }
                return eventList;
        }
        public void approveEvents(int eventId)
                        throws Exception {
                Class.forName(Baseconnection.nameClass);
                Connection con = DriverManager.getConnection(Baseconnection.url, Baseconnection.username,
                                Baseconnection.password);
                PreparedStatement ps = con.prepareStatement(
                                "UPDATE EVENT SET is_approve = 1 WHERE event_id = ?");
                ps.setInt(1, eventId);

                ps.executeUpdate();
                ps.close();

        }

}
