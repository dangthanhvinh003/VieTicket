package com.example.VieTicketSystem.model.service;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretGenerator {
    public static String generateSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte random[] = new byte[32];
        secureRandom.nextBytes(random);
        return Base64.getEncoder().encodeToString(random);
    }
}
