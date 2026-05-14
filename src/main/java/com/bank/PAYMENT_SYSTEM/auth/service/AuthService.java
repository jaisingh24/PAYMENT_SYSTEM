package com.bank.PAYMENT_SYSTEM.auth.service;


import com.bank.PAYMENT_SYSTEM.auth.dto.AuthResponse;
import com.bank.PAYMENT_SYSTEM.auth.dto.LoginRequest;
import com.bank.PAYMENT_SYSTEM.auth.dto.RegisterRequest;
import com.bank.PAYMENT_SYSTEM.common.enums.Role;
import com.bank.PAYMENT_SYSTEM.exception.ResourceNotFoundException;
import com.bank.PAYMENT_SYSTEM.security.JwtService;
import com.bank.PAYMENT_SYSTEM.user.entity.User;
import com.bank.PAYMENT_SYSTEM.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // REGISTER

    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {

            throw new RuntimeException(
                    "Phone number already exists");
        }

        User user = new User();

        user.setFullName(request.getFullName());

        user.setEmail(request.getEmail());

        user.setPhoneNumber(request.getPhoneNumber());

        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);

        user.setEnabled(true);

        userRepository.save(user);

        return "User registered successfully";
    }

    // LOGIN

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(
                        request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invalid credentials"));

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash());

        if (!passwordMatches) {
            throw new RuntimeException(
                    "Invalid credentials");
        }

        String token =
                jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }
}