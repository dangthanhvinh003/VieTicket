package com.example.VieTicketSystem.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private int eventId;
    private String name;
    private String description;
    private LocalDateTime  startDate;
    private String location;
    private String type;
    private LocalDateTime  ticketSaleDate;
    private LocalDateTime  endDate;
    private Organizer organizer;
    private String poster;
    private String banner;
    private int approved;
    private List<Area> areas; // Danh sách các khu vực và giá tiền
    private SeatMap seatMap; // Ảnh của sơ đồ chỗ ngồi
    private int view;
    public Event(int eventId, String name, String description, LocalDateTime  startDate, String location, String type,
    LocalDateTime  ticketSaleDate, LocalDateTime  endDate, Organizer organizer, String poster, String banner, int isApprove, int view) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.location = location;
        this.type = type;
        this.ticketSaleDate = ticketSaleDate;
        this.endDate = endDate;
        this.organizer = organizer;
        this.poster = poster;
        this.banner = banner;
        this.approved = isApprove;
        this.view = view;
    }
    

}
