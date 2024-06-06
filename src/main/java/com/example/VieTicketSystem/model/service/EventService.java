package com.example.VieTicketSystem.model.service;

import com.example.VieTicketSystem.model.entity.Event;
import com.example.VieTicketSystem.model.repo.EventRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepo eventRepository;

    public List<Event> searchEvents(String keyword) {
        return eventRepository.searchEvents(keyword);
    }
}