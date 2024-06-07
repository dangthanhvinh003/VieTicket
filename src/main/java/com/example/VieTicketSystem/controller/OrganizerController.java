package com.example.VieTicketSystem.controller;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cloudinary.Cloudinary;
import com.example.VieTicketSystem.model.entity.AdditionalData;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.EventStatistics;
import com.example.VieTicketSystem.model.entity.Row;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.AreaRepo;
import com.example.VieTicketSystem.model.repo.EventRepo;
import com.example.VieTicketSystem.model.repo.OrganizerRepo;
import com.example.VieTicketSystem.model.repo.RowRepo;
import com.example.VieTicketSystem.model.repo.SeatMapRepo;
import com.example.VieTicketSystem.model.repo.SeatRepo;
import com.example.VieTicketSystem.model.repo.UserRepo;
import com.example.VieTicketSystem.model.service.FileUpload;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrganizerController {

    @Autowired
    EventRepo eventRepo;
    @Autowired
    SeatMapRepo seatMapRepo;
    @Autowired
    AreaRepo areaRepo;
    @Autowired
    RowRepo rowRepo;
    @Autowired
    SeatRepo seatRepo;
    @Autowired
    OrganizerRepo organizerRepo;
    @Autowired
    FileUpload fileUpload;
    @Autowired
    Cloudinary cloudinary;

    @GetMapping(value = ("/createEvent"))
    public String createEventPage(HttpSession httpSession) {
        httpSession.setAttribute("eventCreated", false);
        return "createEvent";
    }

    @GetMapping(value = ("/inactive-account"))
    public String inactiveAccountPage() {
        return "inactive-account";
    }
    @PostMapping(value = ("/viewStatistics"))
    public String statisticsPage(@RequestParam("eventId") int eventId, Model model) {
        EventStatistics eventStatistics = eventRepo.getEventStatisticsByEventId(eventId);    
        Map<String, Double> dailyRevenueMap = eventRepo.getDailyRevenueByEventId(eventId);
        model.addAttribute("eventStatistics", eventStatistics);
        model.addAttribute("dailyStatistics", dailyRevenueMap);
        return "statistics";
    }

    @GetMapping(value = "/viewMyListEvent")
    public String viewMyListEvent(@RequestParam(value = "search", required = false) String search, Model model,
            HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("activeUser");
        List<Event> eventList;
        if (search == null || search.isEmpty()) {
            eventList = eventRepo.getAllEventsByOrganizerId(user.getUserId());
        } else {
            eventList = eventRepo.searchEventByNameAndOrganizerId(search, user.getUserId());
        }
        model.addAttribute("eventList", eventList);
        return "viewMyListEvent";
    }

    @GetMapping(value = "/allEvents")
    public String allEvents(Model model, HttpSession httpSession) {
        model.addAttribute("pageType", "all");
        return viewMyListEvent(null, model, httpSession);
    }

    @GetMapping(value = "/pendingEvents")
    public String pendingEvents(Model model, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("activeUser");
        List<Event> eventList = eventRepo.getAllEventsByOrganizerId(user.getUserId());
        List<Event> pendingEvents = eventList.stream()
                .filter(event -> event.getApproved() == 0 || event.getApproved() == 3)
                .collect(Collectors.toList());
        model.addAttribute("eventList", pendingEvents);
        model.addAttribute("pageType", "pending");
        return "viewMyListEvent";
    }

   
    @GetMapping(value = "/approvedEvents")
    public String approvedEvents(Model model, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("activeUser");
        List<Event> eventList = eventRepo.getAllEventsByOrganizerId(user.getUserId());
        LocalDateTime currentDate = LocalDateTime.now();
        
        List<Event> approvedEvents = eventList.stream()
                .filter(event -> event.getApproved() == 1 && event.getEndDate().isAfter(currentDate))
                .collect(Collectors.toList());
        
        model.addAttribute("eventList", approvedEvents);
        model.addAttribute("pageType", "approved");
        return "viewMyListEvent";
    }

    @GetMapping(value = "/passedEvents")
    public String passedEvents(Model model, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("activeUser");
        List<Event> eventList = eventRepo.getAllEventsByOrganizerId(user.getUserId());
        LocalDateTime currentDate = LocalDateTime.now();
        
        List<Event> passedEvents = eventList.stream()
                .filter(event -> event.getEndDate().isBefore(currentDate))
                .collect(Collectors.toList());
        
        model.addAttribute("eventList", passedEvents);
        model.addAttribute("pageType", "passed");
        return "viewMyListEvent";
    }

    @PostMapping(value = "/eventEditPage")
public String eventEditPage(@RequestParam("eventId") int eventId, Model model, HttpSession httpSession) {
    httpSession.setAttribute("eventIdEdit", eventId);
    Event event = eventRepo.getEventById(eventId);
    model.addAttribute("eventEdit", event);
    return "eventEdit";
}
    @PostMapping(value = ("/eventEditSubmit"))
    public String addEvent(@RequestParam("name") String name, @RequestParam("description") String description,
                           @RequestParam("start_date") LocalDateTime startDate, @RequestParam("location") String location,
                           @RequestParam("type") String type, @RequestParam("ticket_sale_date") LocalDateTime ticketSaleDate,
                           @RequestParam("end_date") LocalDateTime endDate, @RequestParam("poster") MultipartFile multipartFile,
                           @RequestParam("banner") MultipartFile multipartFile1,
                           @RequestParam("currentPoster") String currentPoster,
                           @RequestParam("currentBanner") String currentBanner,
                           HttpSession httpSession, Model model) throws Exception {
        int eventId = (int) httpSession.getAttribute("eventIdEdit");
        String posterUrl = currentPoster;
        String bannerUrl = currentBanner;
    
        if (!multipartFile.isEmpty()) {
            posterUrl = fileUpload.uploadFile(multipartFile);
        }
        model.addAttribute("poster", posterUrl);
    
        if (!multipartFile1.isEmpty()) {
            bannerUrl = fileUpload.uploadFile(multipartFile1);
        }
        model.addAttribute("banner", bannerUrl);
    
        User user = (User) httpSession.getAttribute("activeUser");
    
        eventRepo.updateEvent(eventId, name, description, startDate, location, type, ticketSaleDate, endDate,
                user.getUserId(), posterUrl, bannerUrl);
    
        return "redirect:/editSuccess";
    }

    @PostMapping(value = "/seatMapEditPage")
public String seatMapEditPage(@RequestParam("eventId") int eventId, HttpSession httpSession) {
    httpSession.setAttribute("eventIdEdit", eventId);
    return "seatMapEdit";
}

    @GetMapping(value = "/seatMapDelete")
    public String seatMapDelete(HttpSession httpSession, RedirectAttributes redirectAttributes)
            throws ClassNotFoundException, SQLException {
        int eventId = (int) httpSession.getAttribute("eventIdEdit");

        seatMapRepo.deleteSeatMapByEventId(eventId);
        redirectAttributes.addAttribute("showModal", "false");
        return "redirect:/noModelEditPage";
    }

    @GetMapping(value = "/noModelEditPage")
    public String noModal() {
        return "seatMapEdit";
    }

    @GetMapping(value = "/editSuccess")
    public String editSuccess() {
        return "editSuccess";
    }

    @PostMapping(value = ("/seatMap/NoSeatMapEdit"))
    public String NoSeatMap(@RequestParam("quantity") int total, @RequestParam("price") String price,
            HttpSession httpSession) throws ClassNotFoundException, SQLException, ParseException {
        int idNewEvent = (int) httpSession.getAttribute("eventIdEdit");
        seatMapRepo.addSeatMap(idNewEvent, "NoSeatMap", null);
        areaRepo.addArea("NoSeatMap", total, idNewEvent, price, seatMapRepo.getSeatMapIdByEventRepo(idNewEvent));
        rowRepo.addRow("NoSeatMap", areaRepo.getIdAreaEventId(idNewEvent));
        for (int i = 0; i < total; i++) {
            seatRepo.addSeat(Integer.toString(i), price,
                    rowRepo.getIdRowByAreaId(areaRepo.getIdAreaEventId(idNewEvent)));
        }
        return "redirect:/editSuccess";
    }

    @GetMapping(value = ("/seatMap/SeatMapBetaEdit"))
    public String SeatMapBetaPage() {
        return "SeatMapBetaEdit";
    }

    @PostMapping(value = ("/seatMap/SeatMapBetaEdit"))
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
        int idNewEvent = (int) httpSession.getAttribute("eventIdEdit");
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

}
