package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.entity.Order;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.OrderRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final HttpSession httpSession;
    private final OrderRepo orderRepo;

    public OrderController(HttpSession httpSession, OrderRepo orderRepo) {
        this.httpSession = httpSession;
        this.orderRepo = orderRepo;
    }

    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) throws Exception {
        User user = (User) httpSession.getAttribute("activeUser");
        if (user == null) {
            throw new Exception("User not found");
        }

        // TODO: Implement findAll by user id (with pagination)
        List<Order> orders = orderRepo.findAllByUserId(user.getUserId(), page, size);
        model.addAttribute("orders", orders);
        model.addAttribute("utils", new Utils());

        // TODO: Pagination
        return "orders/list"; // return the view name
    }

    @GetMapping("/view-order")
    public String viewOrder(Model model,
                            @RequestParam int orderId) throws Exception {
        Order order = orderRepo.findById(orderId);
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
}