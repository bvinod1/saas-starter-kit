package com.saasstarterkit.auth.controller;

import com.saasstarterkit.auth.dto.LoginRequest;
import com.saasstarterkit.auth.dto.RegisterRequest;
import com.saasstarterkit.auth.dto.RegisterResponse;
import com.saasstarterkit.auth.exception.UserAlreadyExistsException;
import com.saasstarterkit.auth.exception.ValidationException;
import com.saasstarterkit.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            
            response.put("authenticated", true);
            response.put("provider", oauthToken.getAuthorizedClientRegistrationId());
            response.put("name", oauth2User.getAttribute("name"));
            response.put("email", oauth2User.getAttribute("email"));
            response.put("picture", oauth2User.getAttribute("picture"));
        } else if (authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            response.put("authenticated", true);
            response.put("username", authentication.getName());
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login-success")
    public ResponseEntity<Map<String, Object>> loginSuccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login-failure")
    public ResponseEntity<Map<String, Object>> loginFailure() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login failed");
        response.put("status", "error");
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(RegisterResponse.error(e.getMessage()));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(RegisterResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegisterResponse.error("Registration failed. Please try again."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<RegisterResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            RegisterResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(RegisterResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegisterResponse.error("Login failed. Please try again."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Refresh token is required"));
            }

            var tokenResponse = authService.refreshToken(refreshToken);
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", tokenResponse.getAccessToken());
            response.put("refreshToken", tokenResponse.getRefreshToken());
            response.put("expiresIn", tokenResponse.getExpiresIn());
            response.put("tokenType", tokenResponse.getTokenType());
            response.put("message", "Token refreshed successfully");
            
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token refresh failed. Please try again."));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Token is required"));
            }

            boolean isValid = authService.validateToken(token);
            boolean isExpired = authService.isTokenExpired(token);
            long expirationTime = authService.getTokenExpirationTime(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("expired", isExpired);
            response.put("expirationTime", expirationTime);
            response.put("currentTime", System.currentTimeMillis() / 1000);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token validation failed"));
        }
    }
} 