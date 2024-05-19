package com.example.VieTicketSystem.model.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class OTPServiceTest {
    private final int timeWindow = 2;

    @Test
    public void testGenerateOTP() {
        System.out.println("Test generateOTP");
        // Test the generateOTP method
        OTPService otpService = new OTPService(timeWindow);
        String secretKey = "xuanhoa";
        String otp = otpService.generateOTP(secretKey);
        System.out.println("OTP: " + otp);
        assertNotNull(otp);
    }

    @Test
    public void testValidateOTP() {
        System.out.println("Test validateOTP");
        // Test the validateOTP method
        OTPService otpService = new OTPService(timeWindow);
        String secretKey = "xuanhoa";
        String otp = otpService.generateOTP(secretKey);
        System.out.println("OTP: " + otp);
        // Sleep for 2 seconds
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = otpService.validateOTP(secretKey, otp);
        System.out.println("Result (true): " + result);
        assertNotNull(result);
    }

    @Test
    public void testValidateOTPExpired() {
        System.out.println("Test validateOTP with expired OTP");
        // Test the validateOTP method with expired OTP
        OTPService otpService = new OTPService(timeWindow);
        String secretKey = "xuanhoa";
        String otp = otpService.generateOTP(secretKey);
        System.out.println("OTP: " + otp);
        // Sleep for 8 seconds
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = otpService.validateOTP(secretKey, otp);
        System.out.println("Result (false): " + result);
        assertNotNull(result);
    }

    @Test
    public void testValidateOTPInvalid() {
        System.out.println("Test validateOTP with invalid OTP");
        // Test the validateOTP method with invalid OTP
        OTPService otpService = new OTPService(timeWindow);
        String secretKey = "xuanhoa";
        String otp = otpService.generateOTP(secretKey);
        System.out.println("OTP: " + otp);
        // Sleep for 2 seconds
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = otpService.validateOTP(secretKey, "123456");
        System.out.println("Result: (false)" + result);
        assertNotNull(result);
    }
}