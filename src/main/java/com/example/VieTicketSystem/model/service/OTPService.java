package com.example.VieTicketSystem.model.service;

import org.jboss.aerogear.security.otp.Totp;

public class OTPService {

    public String generateOTP(String secretKey) {
        Totp totp = new Totp(secretKey);
        return totp.now();
    }

    public boolean validateOTP(String secretKey, String userOTP) {
        Totp totp = new Totp(secretKey);
        return totp.verify(userOTP);
    }
}
