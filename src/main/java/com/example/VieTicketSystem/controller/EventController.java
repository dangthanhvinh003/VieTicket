package com.example.VieTicketSystem.controller;

import com.cloudinary.Cloudinary;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Row;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.AreaRepo;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.OrganizerRepo;
import com.example.VieTicketSystem.model.repo.RowRepo;
import com.example.VieTicketSystem.model.repo.SeatMapRepo;
import com.example.VieTicketSystem.model.repo.SeatRepo;
import com.example.VieTicketSystem.model.service.EventService;
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
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.attribute.standard.Chromaticity;

@Controller
public class EventController {

    @Autowired
    EventRepo eventRepo;
    @Autowired
    AreaRepo areaRepo;
    @Autowired
    RowRepo rowRepo;
    @Autowired
    SeatRepo seatRepo;
    @Autowired
    SeatMapRepo seatMapRepo;
    @Autowired
    OrganizerRepo organizerRepo;
    @Autowired
    FileUpload fileUpload;
    @Autowired
    Cloudinary cloudinary;
    @Autowired
    private EventService eventService;

    @PostMapping(value = ("/add-event"))
    public String addEvent(@RequestParam("name") String name, @RequestParam("description") String description,
            @RequestParam("start_date") LocalDateTime startDate, @RequestParam("location") String location,
            @RequestParam("type") String type, @RequestParam("ticket_sale_date") LocalDateTime ticketSaleDate,
            @RequestParam("end_date") LocalDateTime endDate, @RequestParam("poster") MultipartFile multipartFile,
            @RequestParam("banner") MultipartFile multipartFile1, HttpSession httpSession, Model model)
            throws Exception {
        String imageURL = fileUpload.uploadFile(multipartFile);
        model.addAttribute("poster", imageURL);
        String imageURL1 = fileUpload.uploadFile(multipartFile1);
        model.addAttribute("banner", imageURL1);
        User user = (User) httpSession.getAttribute("activeUser");
        System.out.println(user);
        Event event = new Event(0, name, description, startDate, location, type, ticketSaleDate, endDate,
                organizerRepo.findById(user.getUserId()), imageURL, imageURL1, 0, 0);
        httpSession.setAttribute("newEvent", event);
        int idNewEvent = eventRepo.addEvent(name, description, startDate, location, type, ticketSaleDate, endDate,
                user.getUserId(),
                imageURL, imageURL1);
        httpSession.setAttribute("idNewEvent", idNewEvent);
        httpSession.setAttribute("eventCreated", true); // Đặt thuộc tính eventCreated
        return "redirect:/seatMap";
    }

    @GetMapping(value = ("/seatMap"))
    public String SeatMapPage() {
        return "seatMap";
    }

    @PostMapping(value = ("/seatMap/NoSeatMap"))
    public String NoSeatMap(@RequestParam("quantity") int total, @RequestParam("price") String price,
            HttpSession httpSession) throws ClassNotFoundException, SQLException, ParseException {
        int idNewEvent = (int) httpSession.getAttribute("idNewEvent");
        seatMapRepo.addSeatMap(idNewEvent, "NoSeatMap", null);
        areaRepo.addArea("NoSeatMap", total, idNewEvent, price, seatMapRepo.getSeatMapIdByEventRepo(idNewEvent));
        rowRepo.addRow("NoSeatMap", areaRepo.getIdAreaEventId(idNewEvent));
        for (int i = 0; i < total; i++) {
            seatRepo.addSeat(Integer.toString(i), price,
                    rowRepo.getIdRowByAreaId(areaRepo.getIdAreaEventId(idNewEvent)));
        }
        return "createEventSuccess";
    }

    @GetMapping(value = ("/seatMap/SeatMapBeta"))
    public String SeatMapBetaPage() {
        return "SeatMapBeta";
    }

    @PostMapping(value = ("/seatMap/SeatMapBeta"))
    public String SeatMapBetaPage(HttpSession httpSession, @RequestParam("seatMapImg") MultipartFile multipartFile1,
            @RequestParam("additionalData") String additionalDataJson)
            throws Exception {
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
        String imageURL1 = fileUpload.uploadFile(multipartFile1);
        seatMapRepo.addSeatMap(idNewEvent, "SeatMapBeta", imageURL1);
        // add area normal
        if (additionalData.getTotalSelectedSeats() != 0) {
            areaRepo.addArea("Normal", additionalData.getTotalSelectedSeats(), idNewEvent,
                    additionalData.getNormalPrice(), seatMapRepo.getSeatMapIdByEventRepo(idNewEvent));

            ArrayList<String> allSeatNormal = additionalData.getSelectedSeats();
            System.out.println("allSeatNormal : " + allSeatNormal);
            Set<Character> uniqueFirstLetters = new HashSet<>();

            if (additionalData.getSelectedSeats() != null) {
                for (String seat : allSeatNormal) {
                    if (seat != null && !seat.isEmpty()) {
                        uniqueFirstLetters.add(seat.charAt(0));
                    }
                }
                System.out.println("uniqueFirstLetters : " + uniqueFirstLetters);
                ArrayList<Character> uniqueFirstLettersList = new ArrayList<>(uniqueFirstLetters);
                System.out.println("uniqueFirstLettersList : " + uniqueFirstLettersList);
                if (areaRepo.getIdAreaEventId(idNewEvent) != -1) {
                    for (int i = 0; i < uniqueFirstLettersList.size(); i++) {
                        rowRepo.addRow(Character.toString(uniqueFirstLettersList.get(i)),
                                areaRepo.getIdAreaEventIdAndName(idNewEvent, "Normal")); // tiep tuc

                    }
                    if (rowRepo.getAllRowIdsByAreaId(areaRepo.getIdAreaEventIdAndName(idNewEvent, "Normal")) != null) { // get
                                                                                                                        // sai
                        ArrayList<Row> allRow = rowRepo
                                .getAllRowsByAreaId(areaRepo.getIdAreaEventIdAndName(idNewEvent, "Normal"));

                        Map<String, Integer> rowIndexMap = new HashMap<>();
                        for (int i = 0; i < allRow.size(); i++) {
                            rowIndexMap.put(allRow.get(i).getRowName(), i);
                        }

                        // Create a 2D list to store seats by row
                        List<List<String>> seatsByRow = new ArrayList<>(allRow.size());
                        for (int i = 0; i < allRow.size(); i++) {
                            seatsByRow.add(new ArrayList<>());
                        }

                        // Distribute seats into the 2D list
                        for (String seat : allSeatNormal) {
                            String row = seat.substring(0, 1); // Get the row letter
                            int index = rowIndexMap.get(row); // Get the index of the row
                            seatsByRow.get(index).add(seat); // Add seat to the corresponding row list
                        }
                        for (int i = 0; i < seatsByRow.size(); i++) {
                            for (String seat : seatsByRow.get(i)) {
                                seatRepo.addSeat(seat, additionalData.getNormalPrice(), allRow.get(i).getRowId());
                            }
                        }

                    }
                }
            }
            // add row normal and seat

        }

        // vip area
        if (additionalData.getTotalVIPSeats() != 0) {

            areaRepo.addArea("Vip", additionalData.getTotalVIPSeats(), idNewEvent,
                    additionalData.getVipPrice(), seatMapRepo.getSeatMapIdByEventRepo(idNewEvent));
            ArrayList<String> allSeatVip = additionalData.getVipSeats();
            System.out.println("allSeatNormal : " + allSeatVip);
            Set<Character> uniqueFirstLetters = new HashSet<>();

            if (additionalData.getSelectedSeats() != null) {
                for (String seat : allSeatVip) {
                    if (seat != null && !seat.isEmpty()) {
                        uniqueFirstLetters.add(seat.charAt(0));
                    }
                }
                System.out.println("uniqueFirstLetters : " + uniqueFirstLetters);
                ArrayList<Character> uniqueFirstLettersList = new ArrayList<>(uniqueFirstLetters);
                System.out.println("uniqueFirstLettersList : " + uniqueFirstLettersList);
                if (areaRepo.getIdAreaEventId(idNewEvent) != -1) {
                    for (int i = 0; i < uniqueFirstLettersList.size(); i++) {
                        rowRepo.addRow(Character.toString(uniqueFirstLettersList.get(i)),
                                areaRepo.getIdAreaEventIdAndName(idNewEvent, "Vip")); // tiep tuc

                    }
                    if (rowRepo.getAllRowIdsByAreaId(areaRepo.getIdAreaEventIdAndName(idNewEvent, "Vip")) != null) { // get
                                                                                                                     // sai
                        ArrayList<Row> allRow = rowRepo
                                .getAllRowsByAreaId(areaRepo.getIdAreaEventIdAndName(idNewEvent, "Vip"));
                        Map<String, Integer> rowIndexMap = new HashMap<>();
                        for (int i = 0; i < allRow.size(); i++) {
                            rowIndexMap.put(allRow.get(i).getRowName(), i);
                        }

                        // Create a 2D list to store seats by row
                        List<List<String>> seatsByRow = new ArrayList<>(allRow.size());
                        for (int i = 0; i < allRow.size(); i++) {
                            seatsByRow.add(new ArrayList<>());
                        }

                        // Distribute seats into the 2D list
                        for (String seat : allSeatVip) {
                            String row = seat.substring(0, 1); // Get the row letter
                            int index = rowIndexMap.get(row); // Get the index of the row
                            seatsByRow.get(index).add(seat); // Add seat to the corresponding row list
                        }
                        for (int i = 0; i < seatsByRow.size(); i++) {
                            for (String seat : seatsByRow.get(i)) {
                                seatRepo.addSeat(seat, additionalData.getVipPrice(), allRow.get(i).getRowId());
                            }
                        }

                    }
                }
            }
        }

        return "redirect:/createEventSuccess";
    }

    @GetMapping("/viewdetailEvent/{id}")
    public String viewEventDetail(@PathVariable("id") int eventId, Model model) throws Exception {
        Event event = eventRepo.findById(eventId);
        eventRepo.incrementClickCount(event.getEventId());
        model.addAttribute("event", event);
        model.addAttribute("organizer", organizerRepo.getOrganizerByEventId(eventId));
        List<Float> ticketPrices = areaRepo.getTicketPricesByEventId(eventId); // Lấy danh sách giá vé

        // Tìm giá vé thấp nhất
        Float minPrice = null;
        if (!ticketPrices.isEmpty()) {
            minPrice = Collections.min(ticketPrices);
        }
        model.addAttribute("minPrice", minPrice); // Thêm giá vé thấp nhất vào model

        System.out.println(organizerRepo.getOrganizerByEventId(eventId));
        return "viewdetailEvent";
    }

    @PostMapping(value = "/search-event")
    public String searchEvent(@RequestParam("keyword") String keyword, Model model) {
        try {
            List<Event> events = eventService.searchEvents(keyword);
            model.addAttribute("events", events);
        } catch (Exception e) {
            model.addAttribute("error", "Error searching for events: " + e.getMessage());
        }
        return "searchResults";
    }
}
