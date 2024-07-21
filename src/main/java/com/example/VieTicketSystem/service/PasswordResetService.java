package com.example.VieTicketSystem.service;

import com.example.VieTicketSystem.config.JwtUtil;
import com.example.VieTicketSystem.model.dto.UserSecretInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.repo.UserRepo;
import com.example.VieTicketSystem.repo.UserSecretsRepo;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepository;
    private final UserSecretsRepo userSecretsRepo;
    private final OTPService otpService;
    private final EmailService emailService;
    private final int OTP_TIME_WINDOW_MINUTES;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public PasswordResetService(PasswordEncoder passwordEncoder, UserRepo userRepository, UserSecretsRepo userSecretsRepo, OTPService otpService, EmailService emailService, @Value("${OTP_TIME_WINDOW_MINUTES}") int OTP_TIME_WINDOW_MINUTES, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userSecretsRepo = userSecretsRepo;
        this.otpService = otpService;
        this.emailService = emailService;
        this.OTP_TIME_WINDOW_MINUTES = OTP_TIME_WINDOW_MINUTES;
        this.jwtUtil = jwtUtil;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

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

        // Generate a secret key for the user
        UserSecretInfo secretKey = userSecretsRepo.findByUserId(user.getUserId());
        if (secretKey != null) {
            if (secretKey.getCreatedAt() != null && secretKey.getCreatedAt().plusMinutes(OTP_TIME_WINDOW_MINUTES).isAfter(java.time.LocalDateTime.now())) {
                return "OTP already sent to " + censored_email + ". Please check your inbox.";
            }
            userSecretsRepo.deleteSecretKey(user.getUserId());
        }
        String secretString = SecretGenerator.generateSecret();
        userSecretsRepo.insertSecretKey(user.getUserId(), secretString);

        // Generate an OTP for the user
        String otp = otpService.generateOTP(secretString);

        // Send the OTP to the user's email address
        emailService.sendOTP(user.getEmail(), user.getUsername(), otp);

        return "OTP sent to " + censored_email;
    }

    public String verifyOTP(String email, String otp) throws Exception {
        // Check if the user already exists in the database
        User user = userRepository.findByEmail(email);
        user = user == null ? userRepository.findByUsername(email) : user;
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }

        // Check if the user has a secret key
        String secretKey = userSecretsRepo.getSecretKey(user.getUserId());
        if (secretKey == null) {
            ;
            throw new RuntimeException("Secret key not found");
        }

        // Verify the OTP
        if (!otpService.validateOTP(secretKey, otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }
        userSecretsRepo.deleteSecretKey(user.getUserId());

        // Generate a reset token for the user and return it
        return jwtUtil.generateToken(userDetailsServiceImpl.loadUserByUsername(user.getUsername()));
    }

    public void resetPassword(String token, String newPassword) throws Exception {
        // Verify the token
        String userName = jwtUtil.extractUsername(token);
        if (userName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token.");
        }

        // Find the user
        User user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found from token.");
        }

        // Check password strength
        if (!userRepository.isValidPassword(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password not strong enough lah.");
        }

        // Update the user's password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
