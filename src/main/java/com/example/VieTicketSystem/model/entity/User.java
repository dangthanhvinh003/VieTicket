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
    private String email;
    
    
    public User(String fullName, String phone, Date dob, char gender, String email, String username, String password, char role) {
        this.fullName = fullName;
        this.phone = phone;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.username = username;
        this.password = password;  
        this.role = role;    
    }


    
}
