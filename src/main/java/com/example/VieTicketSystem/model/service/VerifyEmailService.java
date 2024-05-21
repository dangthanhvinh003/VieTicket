package com.example.VieTicketSystem.model.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.VieTicketSystem.model.entity.UnverifiedUser;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.UnverifiedUserRepo;
import com.example.VieTicketSystem.model.repo.UserRepo;
import com.example.VieTicketSystem.model.repo.UserSecretsRepo;

@Service
public class VerifyEmailService {
    @Autowired
    EmailService emailService;

    @Value("${OTP_TIME_WINDOW_MINUTES}")
    double timeWindow;

    @Autowired
    UserRepo userRepo;

    @Autowired
    PasswordResetService passwordResetService;

    @Autowired
    UnverifiedUserRepo unverifiedUserRepo;

    @Autowired
    UserSecretsRepo userSecretsRepo;

    @Autowired
    OTPService otpService;

    public boolean isUnverified(String email) throws Exception {
        User user = userRepo.findByEmail(email);
        user = user == null ? userRepo.findByUsername(email) : user;
        if (user == null) {
            throw new Exception("User not exist");
        }

        return unverifiedUserRepo.isUnverified(user.getUserId());
    }

    public String sendOTP(String email) throws Exception {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new Exception("User with email " + email + "not exist.");
        }

        // Check if the user has a secret key
        String secretKey = userSecretsRepo.getSecretKey(user.getUserId());
        if (secretKey == null) {
            // If the user doesn't have a secret key, generate a new one
            secretKey = SecretGenerator.generateSecret();
            userSecretsRepo.insertSecretKey(user.getUserId(), secretKey);
        }

        // Generate an OTP for the user
        String otp = otpService.generateOTP(secretKey);

        // Send the OTP to the user's email address
        emailService.sendOTP(user.getEmail(), user.getUsername(), otp);

        UnverifiedUser unverifiedUser = unverifiedUserRepo.findById(userRepo.findByEmail(email).getUserId());
        if (unverifiedUser != null) {
            // Throw an exception if users do shits too fast
            if (unverifiedUser.getCreatedAt().plusMinutes((int) timeWindow).isAfter(LocalDateTime.now())) {
                throw new Exception("An email with OTP still valid was sent not long ago. Please try again later.");
            }
            // Delete old entry if user do shits too slow
            unverifiedUserRepo.deleteById(unverifiedUser.getUserId());
        }

        unverifiedUser = new UnverifiedUser();
        unverifiedUser.setUserId(userRepo.findByEmail(email).getUserId());
        unverifiedUser.setCreatedAt(LocalDateTime.now());

        unverifiedUserRepo.saveNew(unverifiedUser);

        return "Email sent to " + email.replaceAll("(?<=.).(?=[^@]*?.@)", "*");
    }

    public boolean validateNewUserOTP(String email, String userOTP) throws Exception {
        // Check if the user already exists in the database
        User user = userRepo.findByEmail(email);
        user = user == null ? userRepo.findByUsername(email) : user;
        if (user == null) {
            throw new Exception("User not found");
        }

        // Check if the user has a secret key
        String secretKey = userSecretsRepo.getSecretKey(user.getUserId());
        if (secretKey == null) {
            throw new Exception("Secret key not found");
        }

        // Verify the OTP
        if (!otpService.validateOTP(secretKey, userOTP)) {
            throw new Exception("Invalid OTP");
        }

        unverifiedUserRepo.deleteById(user.getUserId());

        return true;
    }
}
