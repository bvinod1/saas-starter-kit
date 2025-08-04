package com.saasstarterkit.user.service;

import com.saasstarterkit.user.entity.User;
import com.saasstarterkit.user.entity.Role;
import com.saasstarterkit.user.repository.UserRepository;
import com.saasstarterkit.user.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(User user) {
        // Check if user already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if not provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
                Role newRole = new Role("USER", "Default user role");
                return roleRepository.save(newRole);
            });
            user.getRoles().add(userRole);
        }
        
        // Set creation time
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }
    
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByTenantId(String tenantId) {
        return userRepository.findByTenantId(tenantId);
    }
    
    public List<User> getUsersByOrganizationId(Long organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }
    
    public User updateLastLogin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }
    
    public String generatePasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            return token;
        }
        throw new RuntimeException("User not found");
    }
    
    public boolean validatePasswordResetToken(String token) {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getPasswordResetExpires().isAfter(LocalDateTime.now());
        }
        return false;
    }
    
    public void resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPasswordResetExpires().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordResetToken(null);
                user.setPasswordResetExpires(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Password reset token has expired");
            }
        } else {
            throw new RuntimeException("Invalid password reset token");
        }
    }
    
    public String generateEmailVerificationToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setEmailVerificationToken(token);
            user.setEmailVerificationExpires(LocalDateTime.now().plusHours(24));
            userRepository.save(user);
            return token;
        }
        throw new RuntimeException("User not found");
    }
    
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEmailVerificationExpires().isAfter(LocalDateTime.now())) {
                user.setEmailVerified(true);
                user.setEmailVerificationToken(null);
                user.setEmailVerificationExpires(null);
                userRepository.save(user);
                return true;
            } else {
                throw new RuntimeException("Email verification token has expired");
            }
        }
        return false;
    }
    
    public boolean authenticateUser(String usernameOrEmail, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.isEnabled() && passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
} 