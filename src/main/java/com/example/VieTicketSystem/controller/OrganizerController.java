package com.example.VieTicketSystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrganizerController {
    

    @GetMapping (value = ("/createEvent"))
    public String createEventPage(){
        return "createEvent";
    }

    @GetMapping (value = ("/inactive-account"))
    public String inactiveAccountPage(){
        return "inactive-account";
    }
}
