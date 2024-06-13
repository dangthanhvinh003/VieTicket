package com.example.VieTicketSystem.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.VieTicketSystem.model.entity.PasswordResetToken;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.PasswordResetTokenRepository;
import com.example.VieTicketSystem.model.repo.UserRepo;
import com.example.VieTicketSystem.model.repo.UserSecretsRepo;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private UserSecretsRepo userSecretsRepo;
    @Autowired
    private OTPService otpService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Value("${OTP_TIME_WINDOW_MINUTES}")
    private int OTP_TIME_WINDOW_MINUTES;

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

        email = user.getEmail();
        String censored_email = email.replaceAll("(^.).+(.)(?=@)", "$1********$2").replaceAll("(@.).+(.)\\.", "$1****$2.");

        // Check if user have a reset token created less than OTP_TIME_WINDOW_MINUTES
        PasswordResetToken token = tokenRepository.getToken(user.getUserId());
        if (token != null && token.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(OTP_TIME_WINDOW_MINUTES))) {
            return "OTP already sent to " + censored_email + ". Please check your inbox.";
        }

        // Generate a secret key for the user
        String secretKey = userSecretsRepo.getSecretKey(user.getUserId());
        if (secretKey != null) {
            userSecretsRepo.deleteSecretKey(user.getUserId());
        }
        secretKey = SecretGenerator.generateSecret();
        userSecretsRepo.insertSecretKey(user.getUserId(), secretKey);

        // Generate an OTP for the user
        String otp = otpService.generateOTP(secretKey);

        // Send the OTP to the user's email address
        emailService.sendOTP(user.getEmail(), user.getUsername(), otp);

        // Generate a reset token for the user
        String resetToken = UUID.randomUUID().toString();

        // Save the reset token in the database
        tokenRepository.cleanUpExpiredTokens();
        token = new PasswordResetToken();
        token.setUserId(user.getUserId());
        token.setToken(resetToken);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now().plusHours(1)); // The token expires in 1 hour
        tokenRepository.insertToken(token);

        return "OTP sent to " + censored_email;
    }

    public String verifyOTP(String email, String otp) throws Exception {
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
        userSecretsRepo.deleteSecretKey(user.getUserId());

        // Retrieve the reset token from the database
        String resetToken = tokenRepository.getTokenString(user.getUserId());
        if (resetToken == null) {
            throw new Exception("Token not found");
        }

        return resetToken;
    }

    public void resetPassword(String token, String newPassword) throws Exception {
        tokenRepository.cleanUpExpiredTokens();

        // Find the token
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new Exception("Invalid token");
        }

        // Find the user
        User user = userRepository.findById(resetToken.getUserId());
        if (user == null) {
            throw new Exception("User not found");
        }
        String hashedPassword = passwordEncoder.encode(newPassword);

        // Update the user's password
        user.setPassword(hashedPassword);
        userRepository.save(user);

        // Delete the token
        tokenRepository.deleteToken(resetToken.getUserId());
    }
}
