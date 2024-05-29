package com.example.VieTicketSystem.controller;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.UserRepo;
import com.example.VieTicketSystem.model.service.FileUpload;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrganizerController {
   

    @GetMapping(value = ("/createEvent"))
    public String createEventPage() {
        return "createEvent";
    }

    @GetMapping(value = ("/inactive-account"))
    public String inactiveAccountPage() {
        return "inactive-account";
    }

    

}
