package com.example.service.impl;

import com.example.dto.*;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.exception.AppException;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.service.EmailService;
import com.example.config.JwtTokenUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final ConcurrentHashMap<String, OtpChallenge> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void register(RegisterRequest request) {

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new AppException("USERNAME_EXISTS");
        }

        if (userRepository.existsByEmail(email)) {
            throw new AppException("EMAIL_EXISTS");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_USER").build()
                ));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(email);
        user.setEnabled(true);
        user.setRoles(Set.of(role));

        userRepository.save(user);
    }

    @Override
    public JwtResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("WRONG_PASSWORD");
        }

        String token = jwtTokenUtil.generateJwtToken(user.getUsername());

        return new JwtResponse(token, user.getUsername());
    }

    private String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public void forgotPassword(String email) {

        String normalizedEmail = email.trim().toLowerCase();

        userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AppException("EMAIL_NOT_FOUND"));

        String otp = generateOtp();
        otpStorage.put(normalizedEmail, new OtpChallenge(otp, LocalDateTime.now().plusMinutes(15)));

        emailService.sendOtpEmail(normalizedEmail, otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        String normalizedEmail = email.trim().toLowerCase();
        OtpChallenge storedOtp = otpStorage.get(normalizedEmail);

        if (storedOtp == null
                || storedOtp.expiresAt().isBefore(LocalDateTime.now())
                || !storedOtp.code().equals(otp)) {
            throw new AppException("INVALID_OTP");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpStorage.remove(normalizedEmail);
    }

    private record OtpChallenge(String code, LocalDateTime expiresAt) {
    }
}
