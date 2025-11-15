package com.example.demo.service;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.AuthRequest;
import com.example.demo.entity.AuthResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.UserDto;
import com.example.demo.entity.UserEntity;
import com.example.demo.feign.EmailServiceClient;
import com.example.demo.feign.EmailServiceClientfallback;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( UserService.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailServiceClient emailServiceClient;

    private final Map<String, UserEntity> pendingUsers = new HashMap<>();
    private final Map<String, UserEntity> verifiedUsers = new HashMap<>();

    // ✅ Register a new user
    @Retry(name = "user-service")
    @CircuitBreaker(name = "user-service", fallbackMethod = "userFallback")
    @Transactional
    public String registerUser(UserDto userDetails) {
        log.info("Attempt to register {}", userDetails.getEmail());
        String email = userDetails.getEmail();

        Optional<UserEntity> existingUserOpt = userRepo.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();

            if (existingUser.isVerified()) {
                log.info("User already exists {}", email);
                return "User already exists: " + email + " | Name: " + existingUser.getName();
            } else {
                emailServiceClient.sendOtp(email);
                log.info("User already exists but not verified {}", email);
                return "User already exists but not verified. OTP resent.";
            }
        }

        if (userDetails.getPassword() == null || userDetails.getPassword().isBlank()) {
            log.error("Password is required and cannot be blank");
            throw new RuntimeException("Password is required and cannot be blank");
        }

        String hashedPassword = passwordEncoder.encode(userDetails.getPassword());

        UserEntity user = new UserEntity();
        user.setName(userDetails.getName());
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setPhoneNumber(userDetails.getPhonenumber());
        user.setVerified(false);
        user.setRole(Role.USER); // ✅ Default role assigned
        userRepo.save(user);

        emailServiceClient.sendOtp(email);
        log.info("New user registered, OTP sent to {}", email);

        return "OTP sent to " + email + ". Please verify your email.";
    }

    public String userFallback(UserDto userDetails, Throwable t) {
        log.error("Email service is currently unavailable for registration {}", userDetails.getEmail());
        return "Email service is currently unavailable. Please try again later.";
    }

    // ✅ Forgot password request
    @CircuitBreaker(name = "user-service", fallbackMethod = "forgotFallback")
    @Transactional
    public String forgotPassword(String email) {
        Optional<UserEntity> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("No account found with this email: {}", email);
            return "No account found with this email: " + email;
        }

        emailServiceClient.generateforgotOtp(email);
        log.info("OTP sent successfully to {}", email);
        return "OTP sent successfully to " + email;
    }

    public String forgotFallback(String email, Throwable t) {
        log.error("Email service for forgot-password OTP is down for {}", email);
        return "Email service is currently unavailable for forgot-password OTP: " + email;
    }

    // ✅ Reset password
    @CircuitBreaker(name = "user-service", fallbackMethod = "resetFallback")
    @Transactional
    public String resetPassword(String email, String otp, String newPassword) {
        boolean isValid = emailServiceClient.verifyOtp(email, otp);
        if (!isValid) {
            log.warn("Invalid OTP during password reset for {}", email);
            return "Invalid OTP";
        }

        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        log.info("Password reset successful for {}", email);
        return "Password reset successful!";
    }

    public String resetFallback(String email, String otp, String newPassword, Throwable t) {
        log.error("Email service is down during password reset for {}", email);
        return "Unable to reset password right now. Please try again later: " + email;
    }

    // ✅ Verify OTP during registration
    @CircuitBreaker(name = "user-service", fallbackMethod = "verifyOtpFallback")
    @Transactional
    public String verifyOtp(String email, String otp) {
        Boolean isValid = emailServiceClient.verifyOtp(email, otp);
        if (isValid == null || !isValid) {
            log.warn("Invalid OTP for {}", email);
            return "Invalid OTP";
        }

        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found for this email."));

        user.setVerified(true);
        userRepo.save(user);
        log.info("✅ Registration complete for {}", email);

        return "✅ Registration complete for " + email;
    }

    public String verifyOtpFallback(String email, String otp, Throwable t) {
        log.error("OTP verification service down for {}", email);
        return "Email service unavailable. Please recheck OTP and try again for " + email;
    }

    // ✅ Login & generate JWT
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserEntity user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user); // ✅ includes role + email

        log.info("User {} logged in successfully", user.getEmail());
        return new AuthResponse(token);
    }

    // ✅ Logout (stateless)
    public String logout(String token) {
        log.info("Logout successful for token: {}", token);
        return "Logged out successfully";
    }
}
