package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.entity.Order;
import com.example.VieTicketSystem.model.entity.Seat;
import com.example.VieTicketSystem.model.entity.Ticket;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.OrderRepo;
import com.example.VieTicketSystem.model.repo.SeatMapRepo;
import com.example.VieTicketSystem.model.repo.SeatRepo;
import com.example.VieTicketSystem.model.repo.TicketRepo;
import com.example.VieTicketSystem.model.service.OrderService;
import com.example.VieTicketSystem.model.service.PurchaseTicketService;
import com.example.VieTicketSystem.model.service.QRCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/purchase")
public class PurchaseTicketController {

    private final SeatMapRepo seatMapRepo;
    private final HttpSession httpSession;
    private final PurchaseTicketService purchaseTicketService;
    private final OrderService orderService;
    private final SeatRepo seatRepo;
    private final OrderRepo orderRepo;
    private final TicketRepo ticketRepo;
    private final QRCodeService qrCodeService;

    public PurchaseTicketController(SeatMapRepo seatMapRepo, HttpSession httpSession, PurchaseTicketService purchaseTicketService, OrderService orderService, SeatRepo seatRepo, OrderRepo orderRepo, TicketRepo ticketRepo, QRCodeService qrCodeService) {
        this.seatMapRepo = seatMapRepo;
        this.httpSession = httpSession;
        this.purchaseTicketService = purchaseTicketService;
        this.orderService = orderService;
        this.seatRepo = seatRepo;
        this.orderRepo = orderRepo;
        this.ticketRepo = ticketRepo;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/select-tickets")
    public String showForm(@RequestParam("eventId") int eventId,
                           Model model,
                           RedirectAttributes redirectAttributes) throws Exception {

        User activeUser = (User) httpSession.getAttribute("activeUser");
        if (activeUser == null) {
            httpSession.setAttribute("redirect", "/purchase/select-tickets?eventId=" + eventId);
            redirectAttributes.addFlashAttribute("error", "Please login to buy ticket");
            return "redirect:/auth/login";
        } else if (activeUser.getRole() != 'u') {
            return "redirect:/";
        }

        // Check if event exists, if not, return not found exception
        if (!purchaseTicketService.isEventExist(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        // Check for event's ticket sale date
        if (purchaseTicketService.isTicketSaleDateStarted(eventId)) {
            redirectAttributes.addFlashAttribute("error", "Ticket sale date not started");
            return "redirect:/viewdetailEvent/" + eventId;
        }

        // Check if event passed
        if (purchaseTicketService.isEventPassed(eventId)) {
            redirectAttributes.addFlashAttribute("error", "Event has passed");
            return "redirect:/viewdetailEvent/" + eventId;
        }

        model.addAttribute("event", purchaseTicketService.getEventWithAreas(eventId));
        if (seatMapRepo.getSeatMapByEventId(eventId).getImg() == null) {
            model.addAttribute("chooseNumberOfSeats", true);
            model.addAttribute("availableSeats", seatRepo.getAvailableSeatsCount(eventId, Seat.TakenStatus.AVAILABLE.toInteger()));
        } else {
            model.addAttribute("chooseNumberOfSeats", false);
            model.addAttribute("seatMap", seatMapRepo.getSeatMapByEventId(eventId));
        }


        return "purchase/select";
    }

    @PostMapping("/select-tickets")
    public ResponseEntity<String> selectTickets(@RequestBody TicketSelection ticketSelection,
                                                HttpServletRequest request) throws Exception {
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            return new ResponseEntity<>("Please login to buy ticket", HttpStatus.UNAUTHORIZED);
        } else if (user.getRole() != 'u') {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        int eventId = ticketSelection.getEventId();
        List<Integer> selectedSeats = ticketSelection.getSeats();

        // Check if event exists
        if (!purchaseTicketService.isEventExist(eventId)) {
            return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
        }

        // Check if event has passed
        if (purchaseTicketService.isEventPassed(eventId)) {
            return new ResponseEntity<>("Event has passed", HttpStatus.BAD_REQUEST);
        }

        // Check if the event has a seat map
        if (seatMapRepo.getSeatMapByEventId(eventId).getImg() != null) {
            // Check if any of the selected seats are already taken
            if (purchaseTicketService.areSeatsTaken(selectedSeats)) {
                return new ResponseEntity<>("One or more of the selected seats are already taken", HttpStatus.BAD_REQUEST);
            }

            // Set seats taken
            seatRepo.updateSeats(ticketSelection.getSeats(), Seat.TakenStatus.RESERVED);
        } else {
            // Handle the case where the event does not have a seat map
            // Assign the selected number of seats to the user from the pool of available virtual seats
            // This is a placeholder and should be replaced with your actual implementation
            List<Integer> assignedSeats = purchaseTicketService.assignVirtualSeatsToUser(eventId, selectedSeats, user.getUserId());
            if (assignedSeats == null) {
                return new ResponseEntity<>("Not enough available seats", HttpStatus.BAD_REQUEST);
            }
            selectedSeats = assignedSeats;
        }

        // Get the baseURL and the client's IP address
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String clientIp = request.getRemoteAddr();

        // If all checks pass, generate the payment URL
        String paymentUrl = orderService.createOrder(selectedSeats, "Ticket purchase for event " + eventId, baseUrl + "/purchase", clientIp, user);

        // Return the payment URL in the response
        return new ResponseEntity<>(paymentUrl, HttpStatus.OK);
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request) throws Exception {

        // Get all the fields from the request to a dictionary
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName;
            String fieldValue;
            fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }

        Order order = orderService.handlePaymentResponse(fields);

        return switch (order.getStatus()) {
            case SUCCESS -> "redirect:/purchase/purchase-success?orderId=" + order.getOrderId();
            case FAILED -> "redirect:/purchase/purchase-failure?orderId=" + order.getOrderId();
            default -> "redirect:/";
        };
    }

    @GetMapping("/purchase-failure")
    public String showPurchaseFailure(@RequestParam("orderId") int orderId, Model model, RedirectAttributes redirectAttributes) throws Exception {
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            httpSession.setAttribute("redirect", "/purchase/purchase-failure?orderId=" + orderId);
            redirectAttributes.addFlashAttribute("error", "Please login to continue");
            return "redirect:/auth/login";
        } else if (user.getRole() != 'u') {
            return "redirect:/";
        }

        Order order = orderRepo.findById(orderId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (order.getUser().getUserId() != user.getUserId()) {
            return "redirect:/";
        }
        if (order.getStatus().equals(Order.PaymentStatus.SUCCESS)) {
            return "redirect:/purchase/purchase-success?orderId=" + orderId;
        } else if (!order.getStatus().equals(Order.PaymentStatus.FAILED)) {
            return "redirect:/purchase/purchase-failure?orderId=" + orderId;
        }

        List<Ticket> tickets = ticketRepo.findByOrderId(orderId);

        model.addAttribute("order", order);
        model.addAttribute("tickets", tickets);

        return "purchase/failure";
    }

    @GetMapping("/purchase-success")
    public String showPurchaseSuccess(@RequestParam("orderId") int orderId, Model model, RedirectAttributes redirectAttributes) throws Exception {
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            httpSession.setAttribute("redirect", "/purchase/purchase-success?orderId=" + orderId);
            redirectAttributes.addFlashAttribute("error", "Please login to continue");
            return "redirect:/auth/login";
        } else if (user.getRole() != 'u') {
            return "redirect:/";
        }

        Order order = orderRepo.findById(orderId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (order.getUser().getUserId() != user.getUserId()) {
            return "redirect:/";
        }
        if (!order.getStatus().equals(Order.PaymentStatus.SUCCESS)) {
            return "redirect:/purchase/purchase-failure?orderId=" + orderId;
        }

        List<Ticket> tickets = ticketRepo.findByOrderId(orderId);

        // Generate QR Code images for each ticket
        Map<Integer, String> qrCodeImages = new HashMap<>();
        for (Ticket ticket : tickets) {
            String qrCodeImage = qrCodeService.generateQRCodeImageBase64(ticket.getQrCode()); // Replace with your method to generate QR Code image
            qrCodeImages.put(ticket.getTicketId(), qrCodeImage);
        }

        model.addAttribute("order", order);
        model.addAttribute("tickets", tickets);
        model.addAttribute("qrCodeImages", qrCodeImages); // Add qrCodeImages to the model

        return "purchase/success";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class TicketSelection {
        private List<Integer> seats;
        private int eventId;
    }
}
