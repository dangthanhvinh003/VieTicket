package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.dto.OrderWithEvent;
import com.example.VieTicketSystem.model.entity.*;
import com.example.VieTicketSystem.repo.EventRepo;
import com.example.VieTicketSystem.repo.OrderRepo;
import com.example.VieTicketSystem.repo.RefundOrderRepo;
import com.example.VieTicketSystem.repo.TicketRepo;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final HttpSession httpSession;
    private final OrderRepo orderRepo;
    private final EventRepo eventRepo;
    private final TicketRepo ticketRepo;
    private final RefundOrderRepo refundOrderRepo;

    public OrderController(HttpSession httpSession, OrderRepo orderRepo, EventRepo eventRepo, TicketRepo ticketRepo, RefundOrderRepo refundOrderRepo) {
        this.httpSession = httpSession;
        this.orderRepo = orderRepo;
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
        this.refundOrderRepo = refundOrderRepo;
    }

    @GetMapping({"/", ""})
    public String listOrders(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) throws Exception {
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        int offset = page * size;
        List<Order> orders = orderRepo.findByUserId(user.getUserId(), size, offset);
        List<OrderWithEvent> ordersWithEvent = new ArrayList<>(orders.size());

        int totalOrders = orderRepo.countByUserId(user.getUserId());
        int totalPages = (totalOrders + size - 1) / size;

        for (Order order : orders) {
            OrderWithEvent orderWithEvent = new OrderWithEvent();
            orderWithEvent.setOrder(order);
            orderWithEvent.setEvent(eventRepo.findEventByOrderId(order.getOrderId()));
            ordersWithEvent.add(orderWithEvent);
        }

        model.addAttribute("orders", ordersWithEvent);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("utils", new Utils());

        return "orders/list"; // return the view name
    }

    @PostMapping("/request-refund")
    public ResponseEntity<String> requestRefund(@RequestBody RefundRequestEntity refundRequestEntity) throws Exception {

        /*
         *   This project should use some kind of database handler with transaction support in the future
         */

        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Order order = orderRepo.findById(refundRequestEntity.orderId);
        if (order.getUser().getUserId() != user.getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid request: Not your order");
        }

        RefundOrder existingRefundOrder = refundOrderRepo.findByOrderId(refundRequestEntity.orderId);
        if (existingRefundOrder != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid request: Refund request already exists.");
        }

        // Check business rule: Refund requests must be made at least 3 day before event start date
        Event event = eventRepo.findEventByOrderId(refundRequestEntity.orderId);
        if (LocalDateTime.now().isAfter(event.getStartDate().minusDays(3))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid request: Refund request must be made at least 3 days before event start date.");
        }

        if (refundRequestEntity.isTotalRefund()) {
            List<Ticket> tickets = ticketRepo.findByOrderId(refundRequestEntity.getOrderId());
            if (tickets.stream().anyMatch(ticket -> ticket.getStatus() != Ticket.TicketStatus.PURCHASED)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: invalid ticket status (is not PURCHASED).");
            }

            RefundOrder refundOrder = new RefundOrder();
            refundOrder.setCreatedOn(LocalDateTime.now());
            refundOrder.setOrder(order);
            refundOrder.setStatus(RefundOrder.RefundStatus.CREATED);
            refundOrder.setTotal((int) order.getTotal());

            // Nah I won't do rollback transaction on error and stuffs. Let some kind of library do it.
            ticketRepo.updateStatusByOrderId(refundRequestEntity.getOrderId(), Ticket.TicketStatus.PENDING_REFUND.toInteger());
            orderRepo.updateStatus(refundRequestEntity.getOrderId(), Order.PaymentStatus.PENDING_TOTAL_REFUND);
            refundOrderRepo.save(refundOrder);

            return new ResponseEntity<>(HttpStatus.OK);
        }

        List<Ticket> tickets = new ArrayList<>(refundRequestEntity.tickets.size());
        int total = 0;

        for (Integer ticketId : refundRequestEntity.tickets) {
            Ticket ticket = ticketRepo.findById(ticketId);
            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: ticketId not found.");
            }
            if (ticket.getOrder().getOrderId() != order.getOrderId()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: ticket and order mismatch.");
            }
            if (ticket.getStatus() != Ticket.TicketStatus.PURCHASED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: invalid ticket status (is not PURCHASED).");
            }

            tickets.add(ticket);
            total += Math.round(ticket.getSeat().getTicketPrice());
        }

        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setCreatedOn(LocalDateTime.now());
        refundOrder.setOrder(order);
        refundOrder.setStatus(RefundOrder.RefundStatus.CREATED);
        refundOrder.setTotal(total);

        List<Integer> ticketIds = tickets.stream().map(Ticket::getTicketId).toList();
        ticketRepo.setStatusInBulk(ticketIds, Ticket.TicketStatus.PENDING_REFUND.toInteger());
        orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.PENDING_PARTIAL_REFUND);
        refundOrderRepo.save(refundOrder);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/view")
    public String viewOrder(Model model,
                            @RequestParam int orderId,
                            RedirectAttributes redirectAttributes) throws Exception {
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            httpSession.setAttribute("redirect", "/orders/view?orderId=" + orderId);
            redirectAttributes.addFlashAttribute("error", "Please login to continue");
            return "redirect:/auth/login";
        } else if (user.getRole() != 'u') {
            return "redirect:/";
        }

        Order order = orderRepo.findById(orderId);
        model.addAttribute("order", order);

        Event event = eventRepo.findEventByOrderId(orderId);
        model.addAttribute("event", event);

        List<Ticket> tickets = ticketRepo.findByOrderId(orderId);
        model.addAttribute("tickets", tickets);

        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (order.getUser().getUserId() != user.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your Order");
        }
        if (order.getStatus() == Order.PaymentStatus.FAILED || order.getStatus() == Order.PaymentStatus.PENDING) {
            return "purchase/failure";
        }

        // At this point, the payment status should be success.
        RefundOrder refundOrder = refundOrderRepo.findByOrderId(orderId);
        model.addAttribute("refundOrder", refundOrder);

        boolean isReturnable = !LocalDateTime.now().isAfter(event.getStartDate().minusDays(3)) && refundOrder == null;
        model.addAttribute("isReturnable", isReturnable);

        model.addAttribute("utils", new Utils());
        return "orders/view";
    }

    @PostMapping("/rating-exists")
    @ResponseBody
    public Map<String, Boolean> ratingExists(@RequestParam("order_id") int orderId) {
        int ratingId = ticketRepo.findRatingIdByOrderId(orderId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", ratingId != -1);
        return response;
    }

    @PostMapping("/rating")
    public ResponseEntity<String> ratingOrganizer(@RequestParam("order_id") int orderId, @RequestParam("rating") int rating) {
        int organizerId = ticketRepo.findOrganizerIdByOrderId(orderId);
        orderRepo.submitRating(rating, organizerId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body("Rating submitted");
    }

    @GetMapping("/rating")
    public ResponseEntity<String> ratingOrganizer() {
        return ResponseEntity.status(HttpStatus.OK).body("Rating submitted");
    }

    public static class Utils {
        public String formatDate(LocalDateTime dateTime) {
            if (dateTime == null) {
                return "N/A";
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                return dateTime.format(formatter);
            }
        }
    }

    @Data
    public static class RefundRequestEntity {
        private List<Integer> tickets;

        @JsonProperty("orderId")
        private int orderId;

        @JsonProperty("isTotalRefund")
        private boolean isTotalRefund;
    }
}