package com.example.VieTicketSystem.controller;

import com.cloudinary.Cloudinary;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.OrganizerRepo;
import com.example.VieTicketSystem.model.repo.SeatMapRepo;
import com.example.VieTicketSystem.model.service.FileUpload;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

@Controller
public class EventController {

    @Autowired
    EventRepo eventRepo ;
    @Autowired
    SeatMapRepo seatMapRepo;
    @Autowired
    OrganizerRepo organizerRepo ;
    @Autowired
    FileUpload fileUpload;
    @Autowired
    Cloudinary cloudinary;

    @PostMapping(value = ("/add-event"))
    public String addEvent(@RequestParam("name") String name, @RequestParam("description") String description,
            @RequestParam("start_date") Date startDate, @RequestParam("location") String location,
            @RequestParam("type") String type, @RequestParam("ticket_sale_date") Date ticketSaleDate,
            @RequestParam("end_date") Date endDate, @RequestParam("poster") MultipartFile multipartFile,
            @RequestParam("poster") MultipartFile multipartFile1, HttpSession httpSession, Model model)
            throws Exception {
        String imageURL = fileUpload.uploadFile(multipartFile);
        model.addAttribute("poster", imageURL);
        String imageURL1 = fileUpload.uploadFile(multipartFile1);
        model.addAttribute("poster", imageURL1);
        User user = (User) httpSession.getAttribute("activeUser");
        System.out.println(user);
        Event event = new Event(0, name, description, startDate, location, type, ticketSaleDate, endDate, organizerRepo.findById(user.getUserId()), type, imageURL, imageURL1, false);
        httpSession.setAttribute("newEvent", event);
        eventRepo.addEvent(name, description, startDate, location, type, ticketSaleDate, endDate, user.getUserId(),
                imageURL, imageURL1);
        return "redirect:/seatMap";
    }
    @GetMapping(value = ("/seatMap"))
    public String SeatMapPage(){
        return "seatMap";
    }
    @GetMapping(value = ("/seatMap/NoSeatMap"))
    public String NoSeatMap(HttpSession httpSession) throws ClassNotFoundException, SQLException{
        Event event = (Event) httpSession.getAttribute("newEvent");
        Event event2 = eventRepo.getEventByName(event.getName());
        seatMapRepo.addSeatMap(event2.getEventId(), "NoSeatMap", "");
        return "createEventSuccess";
    }
    @GetMapping(value = ("/seatMap/SeatMapBeta"))
    public String SeatMapBetaPage(){
        return "SeatMapBeta";
    }
    @PostMapping(value = ("/seatMap/SeatMapBeta"))
    public String SeatMapBetaPage(HttpSession httpSession,@RequestParam("seatMapImg") MultipartFile multipartFile1) throws ClassNotFoundException, SQLException, IOException{
        Event event = (Event) httpSession.getAttribute("newEvent");
        Event event2 = eventRepo.getEventByName(event.getName());
        String imageURL1 = fileUpload.uploadFile(multipartFile1);
        seatMapRepo.addSeatMap(event2.getEventId(), "SeatMapBeta", imageURL1);
        return "redirect:/createEventSuccess";
    }
    
}
