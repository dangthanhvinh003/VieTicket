package com.example.VieTicketSystem.model.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OTPService {
    private final int timeWindow;

    private static final Map<String, String> newUserOTPs = new ConcurrentHashMap<>();

    public String generateNewUserOTP(String email) {
        String secretKey = SecretGenerator.generateSecret();
        String otp = generateOTP(secretKey);
        newUserOTPs.put(email, secretKey);
        return otp;
    }

    public boolean validateNewUserOTP(String email, String userOTP) {
        String secretKey = newUserOTPs.get(email);
        if (secretKey == null) {
            throw new RuntimeException("No OTP found for email");
        }

        if (!validateOTP(secretKey, userOTP)) {
            return false;
        }

        newUserOTPs.remove(email);
        return true;
    }

    public OTPService(@Value("${OTP_TIME_WINDOW_MINUTES}") double timeWindow) {
        timeWindow *= 60; // Convert minutes to seconds
        if (timeWindow < 45) {
            this.timeWindow = 45;
        } else if (timeWindow > 180) {
            this.timeWindow = 180;
        } else {
            this.timeWindow = (int) timeWindow;
        }

    }

    public String generateOTP(String secretKey) {
        try {
            Clock clock = new Clock(timeWindow); // Or use a synchronized clock in production
            Totp totp = new Totp(secretKey, clock);
            return totp.now();
        } catch (Exception e) {
            // Handle exceptions appropriately (e.g., log, throw custom exception)
            throw new RuntimeException("Error generating OTP", e); // Example
        }
    }

    public boolean validateOTP(String secretKey, String userOTP) {
        try {
            Clock clock = new Clock(timeWindow); // Or use a synchronized clock in production
            Totp totp = new Totp(secretKey, clock);
            return totp.verify(userOTP);
        } catch (Exception e) {
            // Handle exceptions appropriately (e.g., log, throw custom exception)
            throw new RuntimeException("Error validating OTP", e); // Example
        }
    }
}
