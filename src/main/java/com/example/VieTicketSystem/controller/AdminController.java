package com.example.VieTicketSystem.controller;

import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.repo.AdminRepo;

@Controller
public class AdminController {
    @Autowired
    AdminRepo adminRepo = new AdminRepo();


    @GetMapping(value = ("/ViewAllApproveOrganizer"))
    public String approveOrganizerPage(Model model) throws ClassNotFoundException, SQLException {
        ArrayList<Organizer> organizers = adminRepo.viewAllListAprrove();
        model.addAttribute("organizers", organizers);
        return "viewApproveOrganizer";
    }
    @PostMapping(value = ("/approveOrganizer"))
    public String approveOrganizer(@RequestParam("organizerId") int organizerId) throws Exception {
        adminRepo.approveOrganizers(organizerId);
        return "redirect:/ViewAllApproveOrganizer";

    }
    @GetMapping(value = ("/ViewAllApproveEvent"))
    public String approveEventPage(Model model) throws Exception {
        ArrayList<Event> events = adminRepo.viewAllListAprroveEvent();
        model.addAttribute("events", events);
        return "viewApproveEvent";
    }
    @PostMapping(value = ("/approveEvent"))
    public String approveEvent(@RequestParam("eventId") int eventId) throws Exception {
        adminRepo.approveEvents(eventId);
        return "redirect:/ViewAllApproveEvent";

    }

}
