package com.example.VieTicketSystem.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private QRCodeService qrCodeService;

    public TicketController(TicketRepo ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    @GetMapping("/tickets")
    public String listTickets(Model model, HttpSession httpSession, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User activeUser = (User) httpSession.getAttribute("activeUser");
        int userId = activeUser.getUserId();
        int offset = page * size;
        try {
            List<Ticket> tickets = ticketRepo.findByUserId(userId, size, offset);
            List<String> qrCodeImages = new ArrayList<>();
            for (Ticket ticket : tickets) {
                String qrCodeImage = qrCodeService.generateQRCodeImageBase64(ticket.getQrCode());
                qrCodeImages.add(qrCodeImage);
            }

            int totalTickets = ticketRepo.countByUserId(userId);
            int totalPages = (totalTickets + size - 1) / size;

            model.addAttribute("tickets", tickets);
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("qrCodeImages", qrCodeImages);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "tickets";
    }
}
