package com.example.VieTicketSystem.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.VieTicketSystem.model.entity.*;
import com.example.VieTicketSystem.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cloudinary.Cloudinary;
import com.example.VieTicketSystem.model.dto.AdditionalData;
import com.example.VieTicketSystem.model.dto.EventStatistics;
import com.example.VieTicketSystem.service.EmailService;
import com.example.VieTicketSystem.service.FileUpload;
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
    @Autowired
    EmailService emailService;
    @Autowired
    private HttpSession httpSession;
    @Autowired
    private RefundOrderRepo refundOrderRepo;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private TicketRepo ticketRepo;

    @GetMapping(value = ("/createEvent"))
    public String createEventPage(HttpSession httpSession) {
        httpSession.setAttribute("eventCreated", false);
        return "event/create/general";
    }

    @GetMapping(value = ("/inactive-account"))
    public String inactiveAccountPage() {
        return "auth/inactive-account";
    }

    @PostMapping(value = ("/viewStatistics"))
    public String statisticsPage(@RequestParam("eventId") int eventId, Model model, HttpSession session) {
        session.setAttribute("IdEventTolistAllUser", eventId);
        EventStatistics eventStatistics = eventRepo.getEventStatisticsByEventId(eventId);
        Map<String, Double> dailyRevenueMap = eventRepo.getDailyRevenueByEventId(eventId);
        model.addAttribute("eventId", eventId);
        model.addAttribute("eventStatistics", eventStatistics);
        model.addAttribute("dailyStatistics", dailyRevenueMap);
        return "event/view/statistics";
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
        return "event/view/mine";
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
        return "event/view/mine";
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
        return "event/view/mine";
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
        return "event/view/mine";
    }

    @PostMapping(value = "/requestPayment")
    public String requestPayment(@RequestParam("eventId") int eventId, Model model, HttpSession httpSession) {
        boolean result = eventRepo.updateEventApprovalStatus(eventId, 4);
        if (!result) {
            return "404";
        }
        return "redirect:/passedEvents";
    }

    @PostMapping(value = "/eventEditPage")
    public String eventEditPage(@RequestParam("eventId") int eventId, Model model, HttpSession httpSession) {
        httpSession.setAttribute("eventIdEdit", eventId);
        Event event = eventRepo.getEventById(eventId);
        model.addAttribute("eventEdit", event);
        return "event/update/general";
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
            posterUrl = fileUpload.uploadFileImgBannerAndPoster(multipartFile, 720, 958);
        }
        model.addAttribute("poster", posterUrl);

        if (!multipartFile1.isEmpty()) {
            bannerUrl = fileUpload.uploadFileImgBannerAndPoster(multipartFile1, 1280, 720);
        }
        model.addAttribute("banner", bannerUrl);
        User user = (User) httpSession.getAttribute("activeUser");
        if (user.getRole() == 'o') {
            eventRepo.updateEvent(eventId, name, description, startDate, location, type, ticketSaleDate, endDate,
                    posterUrl, bannerUrl, false);
            return "redirect:/editSuccess";
        }
        if (user.getRole() == 'a') {
            eventRepo.updateEvent(eventId, name, description, startDate, location, type, ticketSaleDate, endDate,
                    posterUrl, bannerUrl, true);
            return "redirect:/ViewAllEventOngoing";
        }
        return "/";

    }

    @PostMapping(value = "/seatMapEditPage")
    public String seatMapEditPage(@RequestParam("eventId") int eventId, HttpSession httpSession, Model model)
            throws SQLException {
        httpSession.setAttribute("eventIdEdit", eventId);
        SeatMap editMap = seatMapRepo.getSeatMapByEventId(eventId);
        if (editMap != null) {
            if (editMap.getName().equals("DrawSeatMap")) {
                model.addAttribute("json", editMap.getMapFile());
                return "seatmap/editor";
            }
        }
        return "event/update/seatmap";
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
        return "event/update/seatmap";
    }

    @GetMapping(value = "/editSuccess")
    public String editSuccess() {
        return "event/update/success";
    }

    @PostMapping(value = ("/seatMap/NoSeatMapEdit"))
    public String NoSeatMap(@RequestParam("quantity") int total, @RequestParam("price") String price,
                            HttpSession httpSession) throws Exception {
        int idNewEvent = (int) httpSession.getAttribute("eventIdEdit");
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
        return "redirect:/editSuccess";
    }

    @GetMapping(value = ("/seatMap/SeatMapBetaEdit"))
    public String SeatMapBetaPage() {
        return "seatmap/beta-edit";
    }

    @PostMapping(value = ("/seatMap/SeatMapBetaEdit"))
    public String SeatMapBetaPage(HttpSession httpSession, @RequestParam("seatMapImg") MultipartFile multipartFile1,
                                  @RequestParam("additionalData") String additionalDataJson)
            throws Exception {
        // Đọc dữ liệu JSON
        ObjectMapper objectMapper = new ObjectMapper();
        AdditionalData additionalData = objectMapper.readValue(additionalDataJson, AdditionalData.class);

        int idNewEvent = (int) httpSession.getAttribute("eventIdEdit");
        String imageURL1 = fileUpload.uploadFileSeatMap(multipartFile1);
        seatMapRepo.addSeatMap(idNewEvent, "SeatMapBeta", imageURL1);
        // add area normal
        if (additionalData.getTotalSelectedSeats() != 0) {
            areaRepo.addArea("Normal", additionalData.getTotalSelectedSeats(), idNewEvent,
                    additionalData.getNormalPrice(), seatMapRepo.getSeatMapIdByEventRepo(idNewEvent));

            ArrayList<String> allSeatNormal = additionalData.getSelectedSeats();
            // System.out.println("allSeatNormal : " + allSeatNormal);
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

    @PostMapping(value = "/sendMailToAllUser")
    public String SendMailToAllUser(
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

        return "redirect:/eventUsers";
    }

    /*
     * This method is used to view the list of users who have purchased tickets for
     * an event
     */

    @GetMapping({"/organizer/refund-list", "organizer/refund-list/"})
    public String viewListRefundOrders(@RequestParam int eventId) {
        return "redirect:/organizer/refund-list/to-approve?eventId=" + eventId;
    }

    @GetMapping({"/organizer/refund-list/to-approve", "organizer/refund-list/to-approve/"})
    public String viewListRefundOrdersToApprove(@RequestParam int eventId, Model model) throws Exception {

        // Check if user exists and is an organizer
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getUserRole() != User.UserRole.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have the authority to access this functionality.");
        }

        // Check if event exists and is owned by organizer
        Event event = eventRepo.findById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid request: Event not found.");
        }
        if (event.getOrganizer().getUserId() != user.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid request: Not your event");
        }

        List<RefundOrder> refundOrders = refundOrderRepo
                .findUniqueRefundOrders(RefundOrder.RefundStatus.CREATED.toInteger(), eventId);
        model.addAttribute("refundOrders", refundOrders);
        model.addAttribute("title", "Refund Orders to Approve");
        model.addAttribute("event", event);
        model.addAttribute("backLink", "/viewMyListEvent");

        return "organizer/refund";
    }

    @GetMapping({"/organizer/refund-list/approved", "organizer/refund-list/approved/"})
    public String viewListApprovedRefundOrders(@RequestParam int eventId, Model model) throws Exception {

        // Check if user exists and is an organizer
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getUserRole() != User.UserRole.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have the authority to access this functionality.");
        }

        // Check if event exists and is owned by organizer
        Event event = eventRepo.findById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid request: Event not found.");
        }
        if (event.getOrganizer().getUserId() != user.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid request: Not your event");
        }

        List<RefundOrder> refundOrders = refundOrderRepo
                .findUniqueRefundOrders(RefundOrder.RefundStatus.APPROVED.toInteger(), eventId);
        refundOrders
                .addAll(refundOrderRepo.findUniqueRefundOrders(RefundOrder.RefundStatus.SUCCESS.toInteger(), eventId));
        refundOrders
                .addAll(refundOrderRepo.findUniqueRefundOrders(RefundOrder.RefundStatus.PENDING.toInteger(), eventId));
        refundOrders
                .addAll(refundOrderRepo.findUniqueRefundOrders(RefundOrder.RefundStatus.FAILED.toInteger(), eventId));

        model.addAttribute("refundOrders", refundOrders);
        model.addAttribute("title", "Approved Refund Orders");
        model.addAttribute("event", event);
        model.addAttribute("backLink", "/viewMyListEvent");

        return "organizer/refund";
    }

    @GetMapping({"/organizer/refund-list/rejected", "organizer/refund-list/rejected/"})
    public String viewListRejectedRefundOrders(@RequestParam int eventId, Model model) throws Exception {

        // Check if user exists and is an organizer
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getUserRole() != User.UserRole.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have the authority to access this functionality.");
        }

        // Check if event exists and is owned by organizer
        Event event = eventRepo.findById(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid request: Event not found.");
        }
        if (event.getOrganizer().getUserId() != user.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid request: Not your event");
        }

        List<RefundOrder> refundOrders = refundOrderRepo
                .findUniqueRefundOrders(RefundOrder.RefundStatus.REJECTED.toInteger(), eventId);
        model.addAttribute("refundOrders", refundOrders);
        model.addAttribute("title", "Rejected Refund Orders");
        model.addAttribute("event", event);
        model.addAttribute("backLink", "/viewMyListEvent");

        return "organizer/refund";
    }

    @PostMapping("/organizer/refund/reject")
    public ResponseEntity<String> handleRefundReject(@RequestParam int orderId) throws Exception {

        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        if (user.getUserRole() != User.UserRole.ORGANIZER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have the authority to access this functionality.");
        }

        RefundOrder refundOrder = refundOrderRepo.findByOrderId(orderId);
        if (refundOrder == null || refundOrder.getStatus() != RefundOrder.RefundStatus.CREATED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Refund order not found or not waiting for approval.");
        }

        Event event = eventRepo.findEventByOrderId(orderId);
        if (event.getOrganizer().getUserId() != user.getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your event");
        }

        // Update refund order status
        refundOrder.setStatus(RefundOrder.RefundStatus.REJECTED);
        refundOrder.setApprovedOn(LocalDateTime.now());

        // Revert order and ticket status back to success
        orderRepo.updateStatus(orderId, Order.PaymentStatus.SUCCESS);
        ticketRepo.updateStatusByOrderIdAndStatus(orderId, Ticket.TicketStatus.PENDING_REFUND.toInteger(),
                Ticket.TicketStatus.PURCHASED.toInteger());
        refundOrderRepo.saveApprovalStatus(refundOrder);

        return ResponseEntity.status(HttpStatus.OK).body("Refund request rejected successfully");
    }

    @PostMapping("/organizer/refund/approve")
    public ResponseEntity<String> handleRefundApproval(@RequestParam int orderId) throws Exception {

        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        if (user.getUserRole() != User.UserRole.ORGANIZER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have the authority to access this functionality.");
        }

        RefundOrder refundOrder = refundOrderRepo.findByOrderId(orderId);
        if (refundOrder == null || refundOrder.getStatus() != RefundOrder.RefundStatus.CREATED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Refund order not found or not waiting for approval.");
        }

        Event event = eventRepo.findEventByOrderId(orderId);
        if (event.getOrganizer().getUserId() != user.getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your event");
        }

        // Update refund order status
        refundOrder.setStatus(RefundOrder.RefundStatus.APPROVED);
        refundOrder.setApprovedOn(LocalDateTime.now());
        refundOrderRepo.saveApprovalStatus(refundOrder);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
