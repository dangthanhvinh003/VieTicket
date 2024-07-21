package com.example.VieTicketSystem.model.dto;

import java.util.ArrayList;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalData {
    private int totalSelectedSeats;
    private int totalVIPSeats;
    private ArrayList<String> selectedSeats;
    private ArrayList<String> vipSeats;
    private String normalPrice;  // Thêm trường này
    private String vipPrice;     // Thêm trường này

}
