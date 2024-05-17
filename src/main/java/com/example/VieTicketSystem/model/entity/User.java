package com.example.VieTicketSystem.model.entity;
import java.sql.Date;

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
public class User {
    private int userId;
    private String fullName;
    private String username;
    private String password;
    private String phone;
    private Date dob;
    private char gender;
    private String avatar;
    private char role;
}
