package com.example.VieTicketSystem.model.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OTPServiceTest {

    @Test
    public void testOTP() {
        OTPService otpService = new OTPService();
        String secretKey = "my-secret-key"; // This should be unique per user

        // Generate an OTP
        String otp = otpService.generateOTP(secretKey);

        // Validate the OTP
        boolean isValid = otpService.validateOTP(secretKey, otp);

        assertTrue(isValid, "The OTP should be valid");
    }
}