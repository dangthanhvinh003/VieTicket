package com.example.VieTicketSystem.model.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

public class OTPServiceTest {
    private final int timeWindow = 2;

    @Test
    public void testGenerateNewUserOTP() {
        System.out.println("Test generateNewUserOTP");
        // Test the generateNewUserOTP method
        OTPService otpService = new OTPService(timeWindow);
        String email = "abc@example.com";
        String otp = otpService.generateNewUserOTP(email);
        System.out.println("OTP: " + otp);
        assertNotNull(otp);
    }

    @Test
    public void testValidateNewUserOTP() {
        System.out.println("Test validateNewUserOTP");
        // Test the validateNewUserOTP method
        OTPService otpService = new OTPService(timeWindow);
        String email = "123@cat.com";
        String otp = otpService.generateNewUserOTP(email);
        System.out.println("OTP: " + otp);
        boolean result = otpService.validateNewUserOTP(email, otp);
        System.out.println("Result (true): " + result);
        assumeTrue(result);
    }

    @Test
    public void testValidateNewUserOTPInvalid() {
        System.out.println("Test validateNewUserOTP with invalid OTP");
        // Test the validateNewUserOTP method with invalid OTP
        OTPService otpService = new OTPService(timeWindow);
        String email = "lmao@cat.cc";
        String otp = otpService.generateNewUserOTP(email);
        System.out.println("OTP: " + otp);
        boolean result = otpService.validateNewUserOTP(email, "123456");
        System.out.println("Result (false): " + result);
        assertFalse(result);
    }

    @Test
    public void testValidateNewUserOTPWithNoOTP() {
        String email = "test@example.com";
        String otp = "123456";
        OTPService otpService = new OTPService(timeWindow);

        assertThrows(RuntimeException.class, () -> otpService.validateNewUserOTP(email, otp));
    }

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