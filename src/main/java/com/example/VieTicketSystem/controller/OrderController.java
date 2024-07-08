package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.dto.OrderWithEvent;
import com.example.VieTicketSystem.model.entity.Order;
import com.example.VieTicketSystem.model.entity.Ticket;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.repo.EventRepo;
import com.example.VieTicketSystem.repo.OrderRepo;
import com.example.VieTicketSystem.repo.TicketRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final HttpSession httpSession;
    private final OrderRepo orderRepo;
    private final EventRepo eventRepo;
    private final TicketRepo ticketRepo;

    public OrderController(HttpSession httpSession, OrderRepo orderRepo, EventRepo eventRepo, TicketRepo ticketRepo) {
        this.httpSession = httpSession;
        this.orderRepo = orderRepo;
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
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

        List<Ticket> tickets = ticketRepo.findByOrderId(orderId);
        model.addAttribute("tickets", tickets);

        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (order.getUser().getUserId() != user.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your Order");
        }
        if (!order.getStatus().equals(Order.PaymentStatus.SUCCESS)) {
            return "purchase/failure";
        }

        model.addAttribute("order", order);
        model.addAttribute("utils", new Utils());
        return "orders/view"; // return the view name
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

    @PostMapping("/rating")
    public String ratingOrganizer (@RequestParam("ticket_id") int ticketId, @RequestParam("rating") int rating) {
        // Call the service to process the rating
        

        int orderId = ticketRepo.findOrderIdByTicketId(ticketId);
        int organizerId = ticketRepo.findOrganizerIdByTicketId(ticketId);
        orderRepo.submitRating(rating, organizerId, orderId);
        // Return a success response        
        return "redirect:/";
    }
    @GetMapping("/rating")
    public String ratingOrganizer() {      
        return "redirect:/";
    }
}