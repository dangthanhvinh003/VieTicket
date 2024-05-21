package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.repo.EventRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class EventController {

    @Autowired
    private EventRepo eventRepo;

    // @GetMapping("/")
    // public String listEvents(Model model) {
    //     List<Event> events = eventRepo.getAllEvents();
    //     model.addAttribute("events", events);
    //     return "index"; // Trả về trang home.html
    // }
}
