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

import java.util.Set;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("USERNAME_EXISTS");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("EMAIL_EXISTS");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_USER").build()
                ));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        user.setRoles(Set.of(role));

        userRepository.save(user);
    }

    @Override
    public JwtResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("WRONG_PASSWORD");
        }

        String token = jwtTokenUtil.generateJwtToken(user.getUsername());

        return new JwtResponse(token, user.getUsername());
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("EMAIL_NOT_FOUND"));

        String otp = generateOtp();
        otpStorage.put(email, otp);

        emailService.sendOtpEmail(email, otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        String storedOtp = otpStorage.get(email);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new AppException("INVALID_OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpStorage.remove(email);
    }
}