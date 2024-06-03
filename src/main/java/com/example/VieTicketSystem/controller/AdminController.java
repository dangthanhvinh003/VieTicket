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
import com.example.VieTicketSystem.model.service.EmailService;

@Controller
public class AdminController {
    @Autowired
    AdminRepo adminRepo = new AdminRepo();
    @Autowired
    EmailService emailService = new EmailService();

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

    @PostMapping(value = ("/approveEvent"))
    public String approveEvent(@RequestParam("eventId") int eventId, Model model) throws Exception {
        adminRepo.approveEvents(eventId);

        return "redirect:/ViewAllApproveEvent";

    }

    @PostMapping(value = ("/DeleteEvent"))
    public String deleteEvent(@RequestParam("eventId") int eventId) throws Exception {
        adminRepo.deleteEventById(eventId);
        return "redirect:/ViewAllApproveEvent";
    }

    @GetMapping(value = ("/ViewAllApproveEvent"))
    public String approveEventPage(Model model) throws Exception {
        ArrayList<Event> events = adminRepo.viewAllListApproveEvent();
        model.addAttribute("events", events);
        model.addAttribute("status", "approve");
        return "viewApproveEvent";
    }

    @GetMapping(value = ("/ViewAllRejectEvent"))
    public String rejectEventPage(Model model) throws Exception {
        ArrayList<Event> events = adminRepo.viewAllListRejectEvent();
        model.addAttribute("status", "reject");
        model.addAttribute("events", events);

        return "viewApproveEvent";
    }

    @PostMapping(value = ("/rejectEvent"))
    public String rejectEvent(
            @RequestParam("eventId") int eventId,
            @RequestParam("eventName") String eventName,
            @RequestParam("organizerEmail") String organizerEmail,
            Model model) throws Exception {
        adminRepo.rejectEvents(eventId);
        String subject = "Your event was rejected";
        String conten = "<html><body style='font-family: sans-serif;'>" +
                "<h1 style='color: #007bff;'>VinhTicket Account</h1>" +
                "<p style='font-size: 16px;'>Hello " + organizerEmail + ",</p>" +
                "<p style='font-size: 16px;'>Your event" + eventName + " has been rejected.</p>"
                +
                "<p style='font-size: 24px; font-weight: bold; color: #f00;'>Please edit it within 5 days</p>" +
                "<p style='font-size: 16px;'>If you do not edit, we will be forced to delete your event</p>" +
                "<p style='font-size: 16px;'>Thank you for using VinhTicket!</p>" +
                "<p style='font-size: 14px; color: #777;'>This is an automated email, please do not reply to this email.</p>"
                +
                "<br>" +
                "<p style='font-size: 12px; color: #555;'>(c) Copyright (c) 2024 VinhTicket. All rights reserved</p>"
                +
                "</body></html>";
        emailService.sendEmail(organizerEmail, subject, conten);
        // Bạn có thể thêm logic sử dụng eventName và organizerEmail ở đây nếu cần

        // Chuyển hướng tới trang ViewAllApproveEvent với tham số trạng thái
        return "redirect:/ViewAllApproveEvent";
    }

}
