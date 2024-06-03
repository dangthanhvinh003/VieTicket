package com.example.VieTicketSystem.controller;

import com.example.VieTicketSystem.model.entity.Organizer;
import com.example.VieTicketSystem.model.entity.OrganizerVerificationToken;
import com.example.VieTicketSystem.model.entity.Ticket;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.*;
import com.example.VieTicketSystem.model.service.SecretGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/organizer/")
public class CheckInController {
    private final TicketRepo ticketRepo;
    private final EventRepo eventRepo;
    private final OrganizerRepo organizerRepo;
    private final UserRepo userRepo;
    private final ObjectMapper objectMapper;
    private final OrganizerVerificationTokenRepo organizerVerificationTokenRepo;
    private final PasswordEncoder passwordEncoder;

    private static final long EXPIRATION = 60 * 60 * 24 * 30;

    public CheckInController(TicketRepo ticketRepo, EventRepo eventRepo, OrganizerRepo organizerRepo, UserRepo userRepo, ObjectMapper objectMapper, OrganizerVerificationTokenRepo organizerVerificationTokenRepo, PasswordEncoder passwordEncoder) {
        this.ticketRepo = ticketRepo;
        this.eventRepo = eventRepo;
        this.organizerRepo = organizerRepo;
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
        this.organizerVerificationTokenRepo = organizerVerificationTokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> payload) throws Exception {
        String username = payload.get("username");
        String password = payload.get("password");
        String familiarName = payload.get("familiar_name");
        if (familiarName == null || familiarName.isBlank()) {
            familiarName = "Log in at " + LocalDateTime.now() + " from IP " + payload.get("ip_address");
        }

        User user = userRepo.findByUsername(username);
        if (user == null || user.getRole() != 'o') {
            return ResponseEntity.notFound().build();
        }
        if (!passwordEncoder.matches(password, userRepo.findByUsername(username).getPassword())) {
            return ResponseEntity.badRequest().build();
        }
        Organizer organizer = organizerRepo.findById(user.getUserId());

        String token = SecretGenerator.generateSecret();
        organizerVerificationTokenRepo.insertToken(new OrganizerVerificationToken(0, token, organizer, familiarName, LocalDateTime.now().plusSeconds(EXPIRATION), LocalDateTime.now()));
        String response = objectMapper.writeValueAsString(Map.of("token", token));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> payload) throws Exception {
        String token = payload.get("token");
        OrganizerVerificationToken organizerVerificationToken = organizerVerificationTokenRepo.getToken(token);
        if (organizerVerificationToken == null) {
            return ResponseEntity.notFound().build();
        }
        organizerVerificationTokenRepo.deleteToken(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/query-events")
    public ResponseEntity<String> queryEvents(@RequestBody Map<String, String> payload) throws Exception {
        String token = payload.get("token");
        OrganizerVerificationToken organizerVerificationToken = organizerVerificationTokenRepo.getToken(token);
        if (organizerVerificationToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String response = objectMapper.writeValueAsString(eventRepo.findUpcomingByOrganizerId(organizerVerificationToken.getOrganizer().getUserId()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkin")
    public ResponseEntity<String> checkIn(@RequestBody Map<String, String> payload) throws Exception {
        String token = payload.get("token");
        String qrCode = payload.get("qrCode");
        String eventId = payload.get("eventId");

        // Check if token is valid
        OrganizerVerificationToken organizerVerificationToken = organizerVerificationTokenRepo.getToken(token);
        if (organizerVerificationToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if ticket exists and is not returned
        Ticket ticket = ticketRepo.findByQrCode(qrCode);
        if (ticket == null || ticket.isReturned()) {
            String response = objectMapper.writeValueAsString(Map.of("message", "Ticket not found"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check if ticket is owned by the organizer
        if (ticket.getSeat().getRow().getArea().getEvent().getOrganizer().getUserId() != organizerVerificationToken.getOrganizer().getUserId()) {
            String response = objectMapper.writeValueAsString(Map.of("message", "Ticket not owned by organizer"));
            return ResponseEntity.badRequest().body(response);
        }

        // Check if ticket is for the correct event
        if (eventId != null && !eventId.isBlank() && ticket.getSeat().getRow().getArea().getEvent().getEventId() != (Integer.parseInt(eventId))) {
            String response = objectMapper.writeValueAsString(Map.of("message", "Ticket not for this event"));
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Check in successful");
        response.put("lead_visitor", ticket.getOrder().getUser().getFullName());
        response.put("event", ticket.getSeat().getRow().getArea().getEvent().getName());
        response.put("seat", ticket.getSeat().getRow().getArea().getName() + " " + ticket.getSeat().getNumber());
        response.put("is_checked_in", String.valueOf(ticket.isCheckedIn()));

        if (ticket.isCheckedIn()) {
            response.replace("message", "Already checked in");
            return ResponseEntity.badRequest().body(objectMapper.writeValueAsString(response));
        }

        ticket.setCheckedIn(true);
        ticketRepo.updateExisting(ticket);
        return ResponseEntity.ok(objectMapper.writeValueAsString(response));
    }
}
