package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.SeatMapRepo;
import com.example.VieTicketSystem.model.service.PurchaseTicketService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/purchase")
public class PurchaseTicketController {

    private final SeatMapRepo seatMapRepo;
    private final HttpSession httpSession;
    private final PurchaseTicketService purchaseTicketService;

    public PurchaseTicketController(SeatMapRepo seatMapRepo, HttpSession httpSession, PurchaseTicketService purchaseTicketService) {
        this.seatMapRepo = seatMapRepo;
        this.httpSession = httpSession;
        this.purchaseTicketService = purchaseTicketService;
    }

    @RequestMapping("/select-tickets")
    public String showForm(@RequestParam("eventId") int eventId,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) throws Exception {

        User activeUser = (User) httpSession.getAttribute("activeUser");
        if (activeUser == null) {
            session.setAttribute("redirect", "/purchase/select-tickets?eventId=" + eventId);
            redirectAttributes.addFlashAttribute("error", "Please login to buy ticket");
            return "redirect:/auth/login";
        } else if (activeUser.getRole() != 'u') {
            return "redirect:/";
        }

        // TODO: Check for event's start date

        model.addAttribute("event", purchaseTicketService.getEventWithAreas(eventId));
        model.addAttribute("seatMap", seatMapRepo.getSeatMapByEventId(eventId));

        return "";
    }
}
