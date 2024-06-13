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

    public User(String fullName, String phone, Date dob, char gender, String email, String username, String password,
            char role) {
        this.fullName = fullName;
        this.phone = phone;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User orElseThrow(Object userNotFound) {
        return null;
    }

    public UserRole getUserRole() {
        return UserRole.fromValue(role);
    }

    @Getter
    public enum UserRole {

        ADMIN('a'),

        ORGANIZER('o'),
        UNVERIFIED_ORGANIZER('O'),
        BANNED_ORGANIZER('p'),

        USER('u'),
        UNVERIFIED_USER('U'),
        BANNED_USER('b');


        private final char value;

        UserRole(char value) {
            this.value = value;
        }

        public static UserRole fromValue(char value) {
            for (UserRole role : UserRole.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            return null;
        }
    }
}
