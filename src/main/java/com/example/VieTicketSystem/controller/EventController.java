package com.example.VieTicketSystem.controller;

import java.io.IOException;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.cloudinary.Cloudinary;
import com.example.VieTicketSystem.model.entity.AdditionalData;
import com.example.VieTicketSystem.model.entity.Area;
import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.entity.Row;
import com.example.VieTicketSystem.model.entity.Seat;
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
        String posterURL = fileUpload.uploadFileImgBannerAndPoster(multipartFile, 720, 958); // Kích thước cho poster
        model.addAttribute("poster", posterURL);

        String bannerURL = fileUpload.uploadFileImgBannerAndPoster(multipartFile1, 1280, 720); // Kích thước cho banner
        model.addAttribute("banner", bannerURL);
        User user = (User) httpSession.getAttribute("activeUser");
        // System.out.println(user);
        Event event = new Event(0, name, description, startDate, location, type, ticketSaleDate, endDate,
                organizerRepo.findById(user.getUserId()), posterURL, bannerURL, 0, 0);
        httpSession.setAttribute("newEvent", event);
        int idNewEvent = eventRepo.addEvent(name, description, startDate, location, type, ticketSaleDate, endDate,
                user.getUserId(),
                posterURL, bannerURL);
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
            HttpSession httpSession) throws Exception {
        int idNewEvent = (int) httpSession.getAttribute("idNewEvent");
        seatMapRepo.addSeatMap(idNewEvent, "NoSeatMap", null);
        areaRepo.addArea("NoSeatMap", total, idNewEvent, price, seatMapRepo.getSeatMapIdByEventRepo(idNewEvent));
        rowRepo.addRow("NoSeatMap", areaRepo.getIdAreaEventId(idNewEvent));
        List<Seat> seatsForRow = new ArrayList<>();
        int getIdAreaEvent = areaRepo.getIdAreaEventId(idNewEvent);
        int getIdRow = rowRepo.getIdRowByAreaId(getIdAreaEvent);
        Row row = rowRepo.getRowById(getIdRow);

        for (int i = 0; i < total; i++) {
            seatsForRow.add(new Seat(Integer.toString(i), Float.parseFloat(price), row));
            // seatRepo.addSeat(Integer.toString(i), price,
            // rowRepo.getIdRowByAreaId(areaRepo.getIdAreaEventId(idNewEvent)));
        }
        seatRepo.addSeats(seatsForRow);
        return "createEventSuccess";
    }

    @GetMapping(value = ("/seatMap/SeatMapEditor"))
    public String SeatMapEditor() {
        return "SeatMapEditor";
    }

    @PostMapping(value = "/seatMap/SeatMapEditor")
    public String SeatMapEditor(MultipartHttpServletRequest request, HttpSession session)
            throws SQLException, ClassNotFoundException, ParseException, IOException {
        int eventId = -1;
        if (session.getAttribute("eventIdEdit") != null) {
            eventId = (int) session.getAttribute("eventIdEdit");
            seatMapRepo.deleteSeatMapByEventId(eventId);
        } else if (session.getAttribute("idNewEvent") != null) {
            eventId = (int) session.getAttribute("idNewEvent");
        } else {
            return "index";
        }

        String name = "DrawSeatMap";

        MultipartFile file = request.getFile("file");
        String imageURL = fileUpload.uploadFile(file);
        String shapesJson = request.getParameter("shapes");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> shapesData = objectMapper.readValue(shapesJson, List.class);
        seatMapRepo.addSeatMapWithEditor(eventId, name, imageURL, shapesJson);

        int seatMapId = seatMapRepo.getSeatMapIdByEventRepo(eventId);

        // Part 1: Adding Areas
        List<Area> areas = new ArrayList<>();
        for (Map<String, Object> shapeWrapper : shapesData) {
            Map<String, Object> shape = (Map<String, Object>) shapeWrapper.get("data");
            if ("Area".equals(shape.get("type"))) {
                String areaName = shape.get("name").toString();
                String ticketPrice = shape.get("ticketPrice").toString();
                List<Map<String, Object>> areaShapes = (List<Map<String, Object>>) shape.get("shapes");
                int totalTickets = 0;

                for (Map<String, Object> areaShape : areaShapes) {
                    if ("Row".equals(areaShape.get("type"))) {
                        totalTickets += ((List<Map<String, Object>>) areaShape.get("seats")).size();
                    }
                }

                Area area = new Area(areaName, totalTickets, Float.parseFloat(ticketPrice), eventId, seatMapId);
                areas.add(area);
            }
        }

        areas = areaRepo.addAreas(areas); // Batch insert areas and get their IDs

        // Part 2: Adding Rows
        List<Row> rows = new ArrayList<>();
        for (Map<String, Object> shapeWrapper : shapesData) {
            Map<String, Object> shape = (Map<String, Object>) shapeWrapper.get("data");
            if ("Area".equals(shape.get("type"))) {
                String areaName = shape.get("name").toString();
                Area area = areas.stream().filter(a -> a.getName().equals(areaName)).findFirst().orElse(null);
                if (area == null)
                    continue;

                List<Map<String, Object>> areaShapes = (List<Map<String, Object>>) shape.get("shapes");

                for (Map<String, Object> areaShape : areaShapes) {
                    if ("Row".equals(areaShape.get("type"))) {
                        String rowName = areaShape.get("name").toString();
                        Row row = new Row(rowName, area);
                        rows.add(row);
                    }
                }
            }
        }

        rows = rowRepo.addRows(rows); // Batch insert rows and get their IDs

        // Part 3: Adding Seats
        List<Seat> seats = new ArrayList<>();
        for (Map<String, Object> shapeWrapper : shapesData) {
            Map<String, Object> shape = (Map<String, Object>) shapeWrapper.get("data");
            if ("Area".equals(shape.get("type"))) {
                List<Map<String, Object>> areaShapes = (List<Map<String, Object>>) shape.get("shapes");

                for (Map<String, Object> areaShape : areaShapes) {
                    if ("Row".equals(areaShape.get("type"))) {
                        String rowName = areaShape.get("name").toString();
                        Row row = rows.stream().filter(r -> r.getRowName().equals(rowName)).findFirst().orElse(null);
                        if (row == null)
                            continue;

                        List<Map<String, Object>> seatsList = (List<Map<String, Object>>) areaShape.get("seats");
                        for (Map<String, Object> seat : seatsList) {
                            String seatNumber = seat.get("number").toString();
                            Seat seatObj = new Seat(seatNumber, row.getArea().getTicketPrice(), row);
                            seats.add(seatObj);
                        }
                    }
                }
            }
        }

        seatRepo.addSeats(seats); // Batch insert seats and get their IDs
        System.out.println(session.getAttribute("idNewEvent"));
        if (session.getAttribute("idNewEvent") != null) {
            session.removeAttribute("idNewEvent");
            return "redirect:/createEventSuccess";
        }
        session.removeAttribute("eventIdEdit");
        return "redirect:/editSuccess";
    }

    @GetMapping(value = { "/createEventSuccess" })
    public String createEventSuccessPage() {
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
        // System.out.println("Total Selected Seats: " +
        // additionalData.getTotalSelectedSeats());
        // System.out.println("Total VIP Seats: " + additionalData.getTotalVIPSeats());
        // System.out.println("Selected Seats: " + additionalData.getSelectedSeats());
        // System.out.println("VIP Seats: " + additionalData.getVipSeats());
        // System.out.println("Normal Price: " + additionalData.getNormalPrice());
        // System.out.println("VIP Price: " + additionalData.getVipPrice());
        // add 1 event
        // Event event = (Event) httpSession.getAttribute("newEvent");
        // Event event2 = eventRepo.get(event.getName());
        int idNewEvent = (int) httpSession.getAttribute("idNewEvent");
        String imageURL1 = fileUpload.uploadFileSeatMap(multipartFile1);
        seatMapRepo.addSeatMap(idNewEvent, "SeatMapBeta", imageURL1);
        // add area normal
        if (additionalData.getTotalSelectedSeats() != 0) {
            areaRepo.addArea("Normal", additionalData.getTotalSelectedSeats(), idNewEvent,
                    additionalData.getNormalPrice(), seatMapRepo.getSeatMapIdByEventRepo(idNewEvent));

            ArrayList<String> allSeatNormal = additionalData.getSelectedSeats();
            Set<Character> uniqueFirstLetters = new HashSet<>();

            if (additionalData.getSelectedSeats() != null) {
                for (String seat : allSeatNormal) {
                    if (seat != null && !seat.isEmpty()) {
                        uniqueFirstLetters.add(seat.charAt(0));
                    }
                }
                // System.out.println("uniqueFirstLetters : " + uniqueFirstLetters);
                ArrayList<Character> uniqueFirstLettersList = new ArrayList<>(uniqueFirstLetters);
                // System.out.println("uniqueFirstLettersList : " + uniqueFirstLettersList);
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
                        List<Seat> seatsForRow = new ArrayList<>();
                        for (int i = 0; i < seatsByRow.size(); i++) {

                            for (String seat : seatsByRow.get(i)) {
                                seatsForRow.add(new Seat(seat, Float.parseFloat(additionalData.getNormalPrice()),
                                        allRow.get(i)));
                                // seatRepo.addSeat(seat, additionalData.getVipPrice(),
                                // allRow.get(i).getRowId());
                            }
                            /// adddseat(<Seat>)
                        }
                        seatRepo.addSeats(seatsForRow);

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
            // System.out.println("allSeatNormal : " + allSeatVip);
            Set<Character> uniqueFirstLetters = new HashSet<>();

            if (additionalData.getSelectedSeats() != null) {
                for (String seat : allSeatVip) {
                    if (seat != null && !seat.isEmpty()) {
                        uniqueFirstLetters.add(seat.charAt(0));
                    }
                }
                // System.out.println("uniqueFirstLetters : " + uniqueFirstLetters);
                ArrayList<Character> uniqueFirstLettersList = new ArrayList<>(uniqueFirstLetters);
                // System.out.println("uniqueFirstLettersList : " + uniqueFirstLettersList);
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
                        List<Seat> seatsForRow = new ArrayList<>();
                        for (int i = 0; i < seatsByRow.size(); i++) {

                            for (String seat : seatsByRow.get(i)) {
                                seatsForRow.add(
                                        new Seat(seat, Float.parseFloat(additionalData.getVipPrice()), allRow.get(i)));
                                // seatRepo.addSeat(seat, additionalData.getVipPrice(),
                                // allRow.get(i).getRowId());
                            }
                            /// adddseat(<Seat>)
                        }
                        seatRepo.addSeats(seatsForRow);

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

        // System.out.println(organizerRepo.getOrganizerByEventId(eventId));
        return "viewdetailEvent";
    }

    @GetMapping("/viewAllEvent")
    public String getAllEvents(Model model) {
        List<Event> events = eventRepo.getAllEvents();
        model.addAttribute("events", events);

        // Chuyển hướng tới trang hiển thị danh sách sự kiện
        return "searchResults"; // Tên của template hiển thị danh sách sự kiện
    }

    @GetMapping("/eventUsers")
    public String getUsersByEventId(Model model, HttpSession httpSession) {
        int eventId = (int) httpSession.getAttribute("IdEventTolistAllUser");
        List<User> users = eventRepo.getUsersWithTicketsByEventId(eventId);
        model.addAttribute("users", users);

        return "eventUsers"; // Tên của template hiển thị danh sách người dùng
    }
}
