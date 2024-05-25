package com.example.VieTicketSystem.controller;

import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.repo.AdminRepo;

@Controller
public class AdminController {
    @Autowired
    AdminRepo adminRepo = new AdminRepo();


    @GetMapping(value = ("/ViewAllApprove"))
    public String approvePage(Model model) throws ClassNotFoundException, SQLException {
        ArrayList<Organizer> organizers = adminRepo.viewAllListApprove();
        model.addAttribute("organizers", organizers);
        return "viewApprove";
    }
    @PostMapping(value = ("/approve"))
    public String approveOrganizer(@RequestParam("organizerId") int organizerId) throws Exception {
        adminRepo.approve(organizerId);
        return "redirect:/ViewAllApprove";

    }

}
