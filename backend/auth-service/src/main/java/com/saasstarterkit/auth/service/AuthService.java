package com.saasstarterkit.auth.service;

import com.saasstarterkit.auth.dto.LoginRequest;
import com.saasstarterkit.auth.dto.RegisterRequest;
import com.saasstarterkit.auth.dto.RegisterResponse;
import com.saasstarterkit.auth.entity.User;
import com.saasstarterkit.auth.exception.UserAlreadyExistsException;
import com.saasstarterkit.auth.exception.ValidationException;
import com.saasstarterkit.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate request
        validateRegistrationRequest(request);

        // Check if user already exists
        if (userService.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create user
        String userId = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User();
        user.setId(userId);
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(hashedPassword);
        user.setEnabled(true);
        user.setEmailVerified(false);

        User savedUser = userService.createUser(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        // Create response
        RegisterResponse.UserData userData = new RegisterResponse.UserData(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            token
        );

        return RegisterResponse.success("User registered successfully", userData);
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password and confirm password do not match");
        }

        // Additional validation can be added here
        if (request.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
    }

    @Transactional
    public RegisterResponse login(LoginRequest request) {
        // Find user by email
        User user = userService.findByEmail(request.getEmail())
            .orElseThrow(() -> new ValidationException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid email or password");
        }

        // Check if user is enabled
        if (!user.isEnabled()) {
            throw new ValidationException("Account is disabled. Please contact support.");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Create response
        RegisterResponse.UserData userData = new RegisterResponse.UserData(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            token
        );

        return RegisterResponse.success("Login successful", userData);
    }
} 