package com.saasstarterkit.auth.service;

import com.saasstarterkit.auth.dto.RegisterRequest;
import com.saasstarterkit.auth.dto.RegisterResponse;
import com.saasstarterkit.auth.entity.Role;
import com.saasstarterkit.auth.entity.User;
import com.saasstarterkit.auth.exception.UserAlreadyExistsException;
import com.saasstarterkit.auth.exception.ValidationException;
import com.saasstarterkit.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRequest;
    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPassword("password123");
        validRequest.setConfirmPassword("password123");

        mockRole = new Role();
        mockRole.setId("role-1");
        mockRole.setName("USER");

        Set<Role> roles = new HashSet<>();
        roles.add(mockRole);

        mockUser = new User();
        mockUser.setId("user-1");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPassword("encodedPassword");
        mockUser.setEnabled(true);
        mockUser.setEmailVerified(false);
        mockUser.setRoles(roles);
    }

    @Test
    void register_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        RegisterResponse response = authService.register(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("user-1", response.getData().getId());
        assertEquals("john.doe@example.com", response.getData().getEmail());
        assertEquals("John", response.getData().getFirstName());
        assertEquals("Doe", response.getData().getLastName());
        assertEquals("jwt-token", response.getData().getToken());

        verify(userService).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userService).createUser(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        when(userService.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> authService.register(validRequest)
        );

        assertEquals("User with email john.doe@example.com already exists", exception.getMessage());
        verify(userService).existsByEmail("john.doe@example.com");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void register_WithMismatchedPasswords_ShouldThrowValidationException() {
        // Arrange
        validRequest.setConfirmPassword("differentPassword");

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> authService.register(validRequest)
        );

        assertEquals("Password and confirm password do not match", exception.getMessage());
        verify(userService, never()).existsByEmail(anyString());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void register_WithShortPassword_ShouldThrowValidationException() {
        // Arrange
        validRequest.setPassword("short");
        validRequest.setConfirmPassword("short");

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> authService.register(validRequest)
        );

        assertEquals("Password must be at least 8 characters long", exception.getMessage());
        verify(userService, never()).existsByEmail(anyString());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void register_WithNullFirstName_ShouldThrowValidationException() {
        // Arrange
        validRequest.setFirstName(null);

        // Act & Assert
        assertThrows(
            ValidationException.class,
            () -> authService.register(validRequest)
        );
    }

    @Test
    void register_WithEmptyEmail_ShouldThrowValidationException() {
        // Arrange
        validRequest.setEmail("");

        // Act & Assert
        assertThrows(
            ValidationException.class,
            () -> authService.register(validRequest)
        );
    }

    @Test
    void register_WithInvalidEmail_ShouldThrowValidationException() {
        // Arrange
        validRequest.setEmail("invalid-email");

        // Act & Assert
        assertThrows(
            ValidationException.class,
            () -> authService.register(validRequest)
        );
    }

    @Test
    void register_WithNullRequest_ShouldThrowValidationException() {
        // Act & Assert
        assertThrows(
            ValidationException.class,
            () -> authService.register(null)
        );
    }

    @Test
    void register_WithSpecialCharactersInName_ShouldSucceed() {
        // Arrange
        validRequest.setFirstName("José");
        validRequest.setLastName("O'Connor");
        
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        RegisterResponse response = authService.register(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(userService).createUser(any(User.class));
    }

    @Test
    void register_WithLongNames_ShouldSucceed() {
        // Arrange
        validRequest.setFirstName("VeryLongFirstNameThatExceedsNormalLength");
        validRequest.setLastName("VeryLongLastNameThatExceedsNormalLength");
        
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        RegisterResponse response = authService.register(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(userService).createUser(any(User.class));
    }
} 