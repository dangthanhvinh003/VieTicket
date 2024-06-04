package com.example.VieTicketSystem.controller;

import com.cloudinary.Cloudinary;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Row;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.AreaRepo;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.OrganizerRepo;
import com.example.VieTicketSystem.model.repo.RowRepo;
import com.example.VieTicketSystem.model.repo.SeatMapRepo;
import com.example.VieTicketSystem.model.repo.SeatRepo;
import com.example.VieTicketSystem.model.repo.UserRepo;
import com.example.VieTicketSystem.model.service.FileUpload;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import com.example.VieTicketSystem.model.entity.AdditionalData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.print.attribute.standard.Chromaticity;

@Controller
public class EventController {

    @Autowired
    EventRepo eventRepo = new EventRepo();
    @Autowired
    AreaRepo areaRepo = new AreaRepo();
    @Autowired
    RowRepo rowRepo = new RowRepo();
    @Autowired
    SeatRepo seatRepo = new SeatRepo();
    @Autowired
    SeatMapRepo seatMapRepo;
    @Autowired
    OrganizerRepo organizerRepo;
    @Autowired
    FileUpload fileUpload;
    @Autowired
    Cloudinary cloudinary;
    @Autowired
    UserRepo userRepo;

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
        Event event = new Event(0, name, description, startDate, location, type, ticketSaleDate, endDate,
                organizerRepo.findById(user.getUserId()), type, imageURL, imageURL1, false);
        httpSession.setAttribute("newEvent", event);
        int idNewEvent = eventRepo.addEvent(name, description, startDate, location, type, ticketSaleDate, endDate, user.getUserId(),
                imageURL, imageURL1);
                httpSession.setAttribute("idNewEvent", idNewEvent);
        return "redirect:/seatMap";
    }

    @GetMapping(value = ("/seatMap"))
    public String SeatMapPage() {
        return "seatMap";
    }

    @GetMapping(value = ("/seatMap/NoSeatMap"))
    public String NoSeatMap(HttpSession httpSession) throws ClassNotFoundException, SQLException {
        int idNewEvent = (int) httpSession.getAttribute("idNewEvent");
        seatMapRepo.addSeatMap(idNewEvent, "NoSeatMap", "");
        return "createEventSuccess";
    }

    @GetMapping(value = ("/seatMap/SeatMapBeta"))
    public String SeatMapBetaPage() {
        return "SeatMapBeta";
    }

    @PostMapping(value = ("/seatMap/SeatMapBeta"))
    public String SeatMapBetaPage(HttpSession httpSession, @RequestParam("seatMapImg") MultipartFile multipartFile1,
            @RequestParam("additionalData") String additionalDataJson)
            throws ClassNotFoundException, SQLException, IOException {
        // Đọc dữ liệu JSON
        ObjectMapper objectMapper = new ObjectMapper();
        AdditionalData additionalData = objectMapper.readValue(additionalDataJson, AdditionalData.class);

        // Sử dụng dữ liệu JSON (ví dụ: in ra để kiểm tra)
        System.out.println("Total Selected Seats: " + additionalData.getTotalSelectedSeats());
        System.out.println("Total VIP Seats: " + additionalData.getTotalVIPSeats());
        System.out.println("Selected Seats: " + additionalData.getSelectedSeats());
        System.out.println("VIP Seats: " + additionalData.getVipSeats());
        System.out.println("Normal Price: " + additionalData.getNormalPrice());
        System.out.println("VIP Price: " + additionalData.getVipPrice());
        // add 1 event
        // Event event = (Event) httpSession.getAttribute("newEvent");
        // Event event2 = eventRepo.get(event.getName());
        int idNewEvent = (int) httpSession.getAttribute("idNewEvent");
        System.out.println(idNewEvent + "Id new event");
        String imageURL1 = fileUpload.uploadFile(multipartFile1);
        seatMapRepo.addSeatMap(idNewEvent, "SeatMapBeta", imageURL1);
        // add area normal
        if (additionalData.getTotalSelectedSeats() != 0) {
            areaRepo.addArea("Normal", additionalData.getTotalSelectedSeats(), idNewEvent,additionalData.getNormalPrice());
        }

        // add row normal and seat
        ArrayList<String> allSeatNormal = additionalData.getSelectedSeats();
        System.out.println("allSeatNormal : "+allSeatNormal);
        Set<Character> uniqueFirstLetters = new HashSet<>();
        
        if (additionalData.getSelectedSeats() != null) {
            for (String seat : allSeatNormal) {
                if (seat != null && !seat.isEmpty()) {
                    uniqueFirstLetters.add(seat.charAt(0));
                }
            }
            System.out.println("uniqueFirstLetters : "+uniqueFirstLetters);
            ArrayList<Character> uniqueFirstLettersList = new ArrayList<>(uniqueFirstLetters);
            System.out.println("uniqueFirstLettersList : " + uniqueFirstLettersList);
            if (areaRepo.getIdAreaEventId(idNewEvent) != -1) {
                for (int i = 0; i < uniqueFirstLettersList.size(); i++) {
                    rowRepo.addRow(Character.toString(uniqueFirstLettersList.get(i)),
                            areaRepo.getIdAreaEventId(idNewEvent)); //tiep tuc
                    
                }
                if (rowRepo.getAllRowIdsByAreaId(areaRepo.getIdAreaEventId(idNewEvent)) != null) { // get sai
                    ArrayList<Row> allRow = rowRepo.getAllRowsByAreaId(areaRepo.getIdAreaEventId(idNewEvent));
                    for (int k = 0; k < allRow.size(); k++) {
                        for (int j = 0; j < allSeatNormal.size(); j++) {
                            if (allSeatNormal.get(j).startsWith(allRow.get(k).getRowName())) {
                                seatRepo.addSeat(allSeatNormal.get(j), additionalData.getNormalPrice(), allRow.get(k).getRowId());
                            }
                        }
                    }
                    
                }
            }
        }
        // vip area
        if (additionalData.getTotalVIPSeats() != 0) {
            areaRepo.addArea("Vip", additionalData.getTotalVIPSeats(), idNewEvent,additionalData.getVipPrice());
        }
        ArrayList<String> allSeatVip = additionalData.getVipSeats();

        return "redirect:/createEventSuccess";
    }

    

    @GetMapping("/viewdetailEvent/{id}")
    public String viewEventDetail(@PathVariable("id") int eventId, Model model) throws Exception{
        Event event = eventRepo.findById(eventId);
        model.addAttribute("event", event);
        // model.addAttribute("organizerAsUser", userRepo.findById((organizerRepo.getOrganizerByEventId(eventId).getUserId())));
        model.addAttribute("organizer", organizerRepo.getOrganizerByEventId(eventId));
        System.out.println(organizerRepo.getOrganizerByEventId(eventId));
        return "viewdetailEvent";
    }
   

}
