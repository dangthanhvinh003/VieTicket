package com.example.VieTicketSystem.model.service;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;

public class SecretGenerator {
    public static String generateSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[160]; // Adjusted the size to 32 bytes (256 bits) for Base32
        secureRandom.nextBytes(randomBytes);

        // Convert to Base32 string
        Base32 base32 = new Base32();
        String base32Secret = base32.encodeToString(randomBytes);
        return base32Secret;
    }
}
