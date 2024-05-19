package com.example.VieTicketSystem.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.UserRepo;
import com.example.VieTicketSystem.model.repo.UserSecretsRepo;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepo userRepository;
    @Autowired
    private UserSecretsRepo userSecretsRepo;
    @Autowired
    private OTPService otpService;
    @Autowired
    private EmailService emailService;

    public String handleFormSubmission(String email) throws Exception {
        // Check if the user already exists in the database
        User user = userRepository.findByEmail(email);
        user = user == null ? userRepository.findByUsername(email) : user;
        if (user == null) {
            // If the user doesn't exist, create a new user
            user = new User();
            user.setEmail(email);
            userRepository.save(user);
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

        String censored_email = user.getEmail().replaceAll("(?<=.).(?=[^@]*?.@)", "*");
        return censored_email;
    }

    public void verifyOTP(String email, String otp) throws Exception {
        // Check if the user already exists in the database
        User user = userRepository.findByEmail(email);
        user = user == null ? userRepository.findByUsername(email) : user;
        if (user == null) {
            throw new Exception("User not found");
        }

        // Check if the user has a secret key
        String secretKey = userSecretsRepo.getSecretKey(user.getUserId());
        if (secretKey == null) {
            throw new Exception("Secret key not found");
        }

        // Verify the OTP
        if (!otpService.validateOTP(secretKey, otp)) {
            throw new Exception("Invalid OTP");
        }
    }
}
