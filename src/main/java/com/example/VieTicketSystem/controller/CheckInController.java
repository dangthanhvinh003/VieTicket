package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.entity.Ticket;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/organizer/")
public class CheckInController {
    private final TicketRepo ticketRepo;
    private final UserRepo userRepo;
    private final ObjectMapper objectMapper;

    public CheckInController(TicketRepo ticketRepo, UserRepo userRepo, ObjectMapper objectMapper) {
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/checkin")
    public ResponseEntity<String> checkIn(@RequestBody Map<String, String> payload) throws Exception {

        // Extract the user's details from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Load the User entity from the database
        User user = userRepo.findByUsername(username);
        if (user == null) {
            String response = objectMapper.writeValueAsString(Map.of("message", "User not found"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check if the user is an organizer
        if (user.getUserRole() != User.UserRole.ORGANIZER) {
            String response = objectMapper.writeValueAsString(Map.of("message", "User is not an organizer"));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        String qrCode = payload.get("qrCode");

        // Check if ticket exists and was purchased successfully and not returned
        Ticket ticket = ticketRepo.findByQrCode(qrCode);
        if (ticket == null || ticket.getStatus() != Ticket.TicketStatus.PURCHASED && ticket.getStatus() != Ticket.TicketStatus.CHECKED_IN) {
            String response = objectMapper.writeValueAsString(Map.of("message", "Ticket not found"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check if event has passed
        if (ticket.getSeat().getRow().getArea().getEvent().getEndDate().isBefore(java.time.LocalDateTime.now())) {
            String response = objectMapper.writeValueAsString(Map.of("message", "Event has passed"));
            return ResponseEntity.badRequest().body(response);
        }

        // Check if ticket is owned by the organizer
        if (ticket.getSeat().getRow().getArea().getEvent().getOrganizer().getUserId() != user.getUserId()) {
            String response = objectMapper.writeValueAsString(Map.of("message", "Ticket not owned by organizer"));
            return ResponseEntity.badRequest().body(response);
        }

        // If passed all checks, include the ticket details in the response
        Map<String, String> response = new java.util.HashMap<>();
        response.put("leadVisitor", ticket.getOrder().getUser().getFullName());
        response.put("event", ticket.getSeat().getRow().getArea().getEvent().getName());
        response.put("seat", ticket.getSeat().getRow().getArea().getName() + " " + ticket.getSeat().getNumber());
        response.put("status", String.valueOf(ticket.getStatus()));

        // Return 400 if ticket is already checked in
        if (ticket.getStatus() == Ticket.TicketStatus.CHECKED_IN) {
            response.put("message", "Ticket already checked in");
            return ResponseEntity.badRequest().body(objectMapper.writeValueAsString(response));
        }

        response.put("message", "Check in successful");
        ticket.setStatus(Ticket.TicketStatus.CHECKED_IN);
        ticketRepo.updateExisting(ticket);

        // Return 200 if check in is successful
        return ResponseEntity.ok(objectMapper.writeValueAsString(response));
    }
}
