package com.example.VieTicketSystem.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.VieTicketSystem.model.dto.AdminStatistics;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.dto.TableAdminStatistics;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.repo.AdminRepo;
import com.example.VieTicketSystem.repo.EventRepo;
import com.example.VieTicketSystem.repo.UserRepo;
import com.example.VieTicketSystem.service.EmailService;

@Controller
public class AdminController {
    @Autowired
    AdminRepo adminRepo = new AdminRepo();
    @Autowired
    EmailService emailService;
    @Autowired
    UserRepo userRepo = new UserRepo();
    @Autowired
    EventRepo eventRepo = new EventRepo();

    @GetMapping(value = ("/ViewAllApproveOrganizer"))
    public String approveOrganizerPage(Model model) throws ClassNotFoundException, SQLException {
        ArrayList<Organizer> organizers = adminRepo.viewAllListAprrove();
        model.addAttribute("organizers", organizers);
        return "admin/users/organizers-pending";
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

    @GetMapping(value = "/ViewAllApproveEvent")
    public String approveEventPage(Model model) throws Exception {
        ArrayList<Event> events = adminRepo.viewAllListApproveEvent();

        // Sort events by ID in descending order
        List<Event> sortedEvents = events.stream()
                .sorted(Comparator.comparingLong(Event::getEventId).reversed())
                .collect(Collectors.toList());

        model.addAttribute("events", sortedEvents);
        model.addAttribute("status", "approve");
        return "admin/events/pending-rejected";
    }

    @GetMapping(value = ("/ViewAllRejectEvent"))
    public String rejectEventPage(Model model) throws Exception {
        ArrayList<Event> events = adminRepo.viewAllListRejectEvent();
        model.addAttribute("status", "reject");
        model.addAttribute("events", events);

        return "admin/events/pending-rejected";
    }

    @PostMapping(value = "/rejectEvent")
    public String rejectEvent(
            @RequestParam("eventId") int eventId,
            @RequestParam("eventName") String eventName,
            @RequestParam("organizerEmail") String organizerEmail,
            @RequestParam("corrections") List<String> corrections,
            Model model) throws Exception {

        // Gọi repository để reject event
        adminRepo.rejectEvents(eventId);
        // Tạo nội dung email với các mục cần sửa
        String subject = "Your event was rejected";
        String conten = "<html><body style='font-family: sans-serif;'>" +
                "<h1 style='color: #007bff;'>VinhTicket Account</h1>" +
                "<p style='font-size: 16px;'>Hello " + organizerEmail + ",</p>" +
                "<p style='font-size: 16px;'>Your event <strong>" + eventName
                + "</strong> has been rejected. Please make the following corrections:</p>";

        // Thêm các mục cần sửa vào nội dung email
        conten += "<ul style='font-size: 16px;'>";
        for (String correction : corrections) {
            conten += "<li>" + correction + "</li>";
        }
        conten += "</ul>";

        conten += "<p style='font-size: 24px;'>Please edit it within <strong style='font-weight: bold; color: #f00;'>5 days</strong></p>"
                +
                "<p style='font-size: 16px;'>If you do not make the necessary changes, we will be forced to delete your event.</p>"
                +
                "<p style='font-size: 16px;'>Thank you for using VinhTicket!</p>" +
                "<p style='font-size: 14px; color: #777;'>This is an automated email, please do not reply to this email.</p>"
                +
                "<br>" +
                "<p style='font-size: 12px; color: #555;'>(c) 2024 VinhTicket. All rights reserved</p>" +
                "</body></html>";

        // Gửi email thông báo
        emailService.sendEmail(organizerEmail, subject, conten);

        // Chuyển hướng tới trang ViewAllApproveEvent với tham số trạng thái
        return "redirect:/ViewAllApproveEvent";
    }

    @GetMapping(value = "admin/dashboard")
    public String dashboardPage(Model model) throws Exception {
        AdminStatistics statistics = adminRepo.getStatisticsForAdmin();
        List<Integer> dailyRevenue = adminRepo.getDailyRevenue();
        List<Integer> monthlyRevenue = adminRepo.getMonthlyRevenue();
        List<TableAdminStatistics> eventRevenues = adminRepo.getEventRevenues(); // Thêm dòng này
        List<TableAdminStatistics> sortedEventRevenues = eventRevenues.stream()
                .sorted(Comparator.comparingDouble(TableAdminStatistics::getRevenue).reversed())
                .collect(Collectors.toList());
        // System.out.println("dailyRevenue : " + dailyRevenue);
        // System.out.println("monthlyRevenue : " + monthlyRevenue);

        model.addAttribute("statisticsOfAdmin", statistics);
        model.addAttribute("dailyRevenue", dailyRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("eventRevenues", sortedEventRevenues); // Thêm dòng này

        return "admin/dashboard";
    }

    @GetMapping(value = ("/ViewAllUser"))
    public String allUserPage(Model model) throws Exception {
        ArrayList<User> users = adminRepo.getAllUser();
        model.addAttribute("users", users);
        model.addAttribute("status", "approve");
        return "admin/users/view";
    }

    @GetMapping(value = ("/ViewActiveOrganizer"))
    public String allActiveOrganizer(Model model) throws Exception {
        ArrayList<Organizer> organizers = adminRepo.getAllActiveOrganizer();
        model.addAttribute("organizers", organizers);

        return "admin/users/organizers-active";
    }

    @PostMapping(value = "/lockUser")
    public String lockUsers(@RequestParam("userid") int id, @RequestParam("role") char role, Model model)
            throws Exception {
        adminRepo.lockUser(id, role);
        User user = userRepo.findById(id);
        String subject = "Your VinhTicket Account has been Locked";
        String content = "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px; max-width: 600px; margin: auto;'>"
                +
                "<h1 style='color: #007bff; text-align: center;'>VinhTicket Account</h1>" +
                "<p style='font-size: 16px; color: #333;'>Hello <strong>" + user.getFullName() + "</strong>,</p>" +
                "<p style='font-size: 16px; color: #333;'>We regret to inform you that your account has been <strong style='color: #ff0000;'>locked</strong> due to suspicious activity.</p>"
                +
                "<p style='font-size: 16px; color: #333;'>Please contact our admin at <a href='mailto:support@vinhticket.com' style='color: #007bff; text-decoration: none;'>support@vinhticket.com</a> for further assistance.</p>"
                +
                "<p style='font-size: 16px; color: #333;'>Thank you for using VinhTicket!</p>" +
                "<p style='font-size: 14px; color: #777;'>This is an automated email, please do not reply to this email.</p>"
                +
                "<br>" +
                "<p style='font-size: 12px; color: #555; text-align: center;'>(c) 2024 VinhTicket. All rights reserved</p>"
                +
                "</div>" +
                "</body></html>";

        // Gửi email thông báo
        emailService.sendEmail(user.getEmail(), subject, content);
        ArrayList<User> users = adminRepo.getAllUser();
        model.addAttribute("users", users);
        model.addAttribute("status", "approve");

        return "admin/users/view";
    }

    @PostMapping(value = "/unlockUser")
    public String unlockUsers(@RequestParam("userid") int id, @RequestParam("role") char role, Model model)
            throws Exception {
        adminRepo.unlockUser(id, role);
        User user = userRepo.findById(id);
        String subject = "Your VinhTicket Account has been Unlocked";
        String content = "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px; max-width: 600px; margin: auto;'>"
                +
                "<h1 style='color: #28a745; text-align: center;'>VinhTicket Account</h1>" +
                "<p style='font-size: 16px; color: #333;'>Hello <strong>" + user.getFullName() + "</strong>,</p>" +
                "<p style='font-size: 16px; color: #333;'>We are pleased to inform you that your account has been <strong style='color: #28a745;'>unlocked</strong> and you can now access all features of VinhTicket.</p>"
                +
                "<p style='font-size: 16px; color: #333;'>If you have any questions or need further assistance, please contact our support team at <a href='mailto:support@vinhticket.com' style='color: #007bff; text-decoration: none;'>support@vinhticket.com</a>.</p>"
                +
                "<p style='font-size: 16px; color: #333;'>Thank you for using VinhTicket!</p>" +
                "<p style='font-size: 14px; color: #777;'>This is an automated email, please do not reply to this email.</p>"
                +
                "<br>" +
                "<p style='font-size: 12px; color: #555; text-align: center;'>(c) 2024 VinhTicket. All rights reserved</p>"
                +
                "</div>" +
                "</body></html>";

        // Gửi email thông báo
        emailService.sendEmail(user.getEmail(), subject, content);
        ArrayList<User> banners = adminRepo.getAllBanner();
        model.addAttribute("status", "reject");
        model.addAttribute("users", banners);
        return "admin/users/view";
    }

    @PostMapping(value = "/sendMailToActiveOrganizer")
    public String SendMailToAllActiveOrganizer(
            @RequestParam("Title") String title,
            @RequestParam("content") String content,
            @RequestParam("organizerEmails") List<String> organizerEmails,
            Model model) throws Exception {

        // Tạo tiêu đề email
        String subject = title;

        // Tạo nội dung email với các mục cần sửa
        String emailContent = "<html><body style='font-family: sans-serif;'>" +

                content
                +

                "<p style='font-size: 16px;'>Thank you for using VinhTicket!</p>" +
                "<p style='font-size: 14px; color: #777;'>This is an automated email, please do not reply to this email.</p>"
                +
                "<br>" +
                "<p style='font-size: 12px; color: #555;'>(c) 2024 VinhTicket. All rights reserved</p>" +
                "</body></html>";

        // Gửi email thông báo tới từng người dùng
        for (String email : organizerEmails) {
            emailService.sendEmail(email, subject, emailContent);
        }

        return "redirect:/ViewActiveOrganizer";
    }

    @GetMapping(value = ("/ViewAllBanner"))
    public String allBannerPage(Model model) throws Exception {
        ArrayList<User> banners = adminRepo.getAllBanner();
        model.addAttribute("status", "reject");
        model.addAttribute("users", banners);

        return "admin/users/view";
    }

    @GetMapping(value = ("/ViewAllEventOngoing"))
    public String allEventOngoingPage(Model model) throws Exception {
        List<Event> events = eventRepo.getAllOngoingEvents();
        model.addAttribute("events", events);
        return "admin/events/ongoing";
    }

    @GetMapping(value = "/searchEvents")
    public String searchEvents(@RequestParam("query") String query, Model model) throws Exception {
        List<Event> events = eventRepo.searchEvents(query);
        model.addAttribute("events", events);
        return "admin/events/ongoing";
    }

}
