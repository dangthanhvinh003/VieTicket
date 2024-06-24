package com.example.VieTicketSystem.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.VieTicketSystem.model.entity.Ticket;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.TicketRepo;
import com.example.VieTicketSystem.model.service.QRCodeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TicketController {

    private final TicketRepo ticketRepo;
    private final QRCodeService qrCodeService;
    private final HttpSession httpSession;

    public TicketController(TicketRepo ticketRepo,
                            QRCodeService qrCodeService,
                            HttpSession httpSession) {
        this.ticketRepo = ticketRepo;
        this.qrCodeService = qrCodeService;
        this.httpSession = httpSession;
    }

    @GetMapping("/tickets")
    public String listTickets(Model model, HttpSession httpSession,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) throws Exception {
        User activeUser = (User) httpSession.getAttribute("activeUser");
        int userId = activeUser.getUserId();
        int offset = page * size;

        List<Ticket> tickets = ticketRepo.findByUserId(userId, size, offset);
        tickets.sort(Comparator.comparing(Ticket::getTicketId).reversed());

        int totalTickets = ticketRepo.countByUserId(userId);
        int totalPages = (totalTickets + size - 1) / size;

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("utils", new Utils());

        return "tickets/list";
    }

    @GetMapping("/tickets/view-ticket")
    public String viewTicket(Model model,
                             @RequestParam int ticketId) throws Exception {
        User activeUser = (User) httpSession.getAttribute("activeUser");
        if (activeUser == null) {
            throw new AccessDeniedException("Access denied");
        }

        Ticket ticket = ticketRepo.findById(ticketId);
        if (ticket == null || ticket.getOrder().getUser().getUserId() != activeUser.getUserId()) {
            throw new AccessDeniedException("Not your ticket");
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("qrCode", qrCodeService.generateQRCodeImageBase64(ticket.getQrCode()));
        model.addAttribute("utils", new Utils());
        return "tickets/view";
    }

    public static class Utils {
        public String formatDate(LocalDateTime dateTime) {
            if (dateTime == null) {
                return "Not purchased";
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                return dateTime.format(formatter);
            }
        }
    }
}
