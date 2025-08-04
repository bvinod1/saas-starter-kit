package com.saasstarterkit.auth.service;

import com.saasstarterkit.auth.entity.Role;
import com.saasstarterkit.auth.entity.User;
import com.saasstarterkit.auth.repository.RoleRepository;
import com.saasstarterkit.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public User createUser(User user) {
        // Assign default USER role if no roles are set
        if (user.getRoles().isEmpty()) {
            Role userRole = findOrCreateRole("USER");
            user.addRole(userRole);
        }
        
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    private Role findOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setId(UUID.randomUUID().toString());
                    newRole.setName(roleName);
                    newRole.setDescription("Default " + roleName + " role");
                    newRole.setCreatedAt(java.time.LocalDateTime.now());
                    newRole.setUpdatedAt(java.time.LocalDateTime.now());
                    return roleRepository.save(newRole);
                });
    }
} 