package com.example.VieTicketSystem.model.service;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;

public class SecretGenerator {
    public static String generateSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[96];
        secureRandom.nextBytes(randomBytes);

        // Convert to Base32 string
        Base32 base32 = new Base32();
        return base32.encodeToString(randomBytes);
    }
}
