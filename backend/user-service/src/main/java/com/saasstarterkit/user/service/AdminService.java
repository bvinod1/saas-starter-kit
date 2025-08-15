package com.saasstarterkit.user.service;

import com.saasstarterkit.user.config.TenantContext;
import com.saasstarterkit.user.dto.AdminUserDTO;
import com.saasstarterkit.user.dto.CreateUserRequest;
import com.saasstarterkit.user.dto.UpdateUserRequest;
import com.saasstarterkit.user.dto.UserRoleDTO;
import com.saasstarterkit.user.entity.TenantUser;
import com.saasstarterkit.user.entity.TenantOrganization;
import com.saasstarterkit.user.entity.UserRole;
import com.saasstarterkit.user.entity.OrganizationMember;
import com.saasstarterkit.user.repository.TenantUserRepository;
import com.saasstarterkit.user.repository.TenantOrganizationRepository;
import com.saasstarterkit.user.repository.UserRoleRepository;
import com.saasstarterkit.user.repository.OrganizationMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;


@Service
@Transactional
public class AdminService {

    @Autowired
    private TenantUserRepository tenantUserRepository;

    @Autowired
    private TenantOrganizationRepository organizationRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    // User Management Methods

    public List<AdminUserDTO> getAllUsers() {
        List<TenantUser> users = tenantUserRepository.findAll();
        return users.stream()
                .map(this::convertToAdminUserDTO)
                .collect(Collectors.toList());
    }

    public AdminUserDTO getUserById(Long userId) {
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToAdminUserDTO(user);
    }

    public AdminUserDTO createUser(CreateUserRequest request) {
        // Check if user already exists
        if (tenantUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user
        TenantUser user = new TenantUser();
        user.setKeycloakUserId("user-" + System.currentTimeMillis());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        TenantUser savedUser = tenantUserRepository.save(user);

        // Assign roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                assignRoleToUser(savedUser.getId(), roleName);
            }
        }

        // Add to organizations if provided
        if (request.getOrganizationIds() != null && !request.getOrganizationIds().isEmpty()) {
            for (Long orgId : request.getOrganizationIds()) {
                addUserToOrganization(savedUser.getId(), orgId);
            }
        }

        return convertToAdminUserDTO(savedUser);
    }

    public AdminUserDTO updateUser(Long userId, UpdateUserRequest request) {
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setStatus(request.getStatus());
        user.setUpdatedAt(LocalDateTime.now());

        TenantUser updatedUser = tenantUserRepository.save(user);

        // Update roles if provided
        if (request.getRoles() != null) {
            // Remove existing roles
            List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
            userRoleRepository.deleteAll(existingRoles);

            // Assign new roles
            for (String roleName : request.getRoles()) {
                assignRoleToUser(userId, roleName);
            }
        }

        // Update organizations if provided
        if (request.getOrganizationIds() != null) {
            // Remove existing organization memberships
            List<OrganizationMember> existingMemberships = organizationMemberRepository.findByUserId(userId);
            organizationMemberRepository.deleteAll(existingMemberships);

            // Add new memberships
            for (Long orgId : request.getOrganizationIds()) {
                addUserToOrganization(userId, orgId);
            }
        }

        return convertToAdminUserDTO(updatedUser);
    }

    public void deleteUser(Long userId) {
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove all roles
        List<UserRole> roles = userRoleRepository.findByUserId(userId);
        userRoleRepository.deleteAll(roles);

        // Remove all organization memberships
        List<OrganizationMember> memberships = organizationMemberRepository.findByUserId(userId);
        organizationMemberRepository.deleteAll(memberships);

        // Delete the user
        tenantUserRepository.delete(user);
    }

    public void activateUser(Long userId) {
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("active");
        user.setUpdatedAt(LocalDateTime.now());
        tenantUserRepository.save(user);
    }

    public void deactivateUser(Long userId) {
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("inactive");
        user.setUpdatedAt(LocalDateTime.now());
        tenantUserRepository.save(user);
    }

    // Role Management Methods

    public List<UserRoleDTO> getAllRoles() {
        List<UserRole> roles = userRoleRepository.findAll();
        return roles.stream()
                .map(this::convertToUserRoleDTO)
                .collect(Collectors.toList());
    }

    public List<UserRoleDTO> getUserRoles(Long userId) {
        List<UserRole> roles = userRoleRepository.findByUserId(userId);
        return roles.stream()
                .map(this::convertToUserRoleDTO)
                .collect(Collectors.toList());
    }

    public void assignRoleToUser(Long userId, String roleName) {
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if role already assigned
        if (userRoleRepository.findByUserIdAndRoleName(userId, roleName).isPresent()) {
            throw new RuntimeException("Role already assigned to user");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleName(roleName);
        userRole.setAssignedAt(LocalDateTime.now());
        userRole.setAssignedBy("admin"); // TODO: Get from current user context

        userRoleRepository.save(userRole);
    }

    public void removeRoleFromUser(Long userId, String roleName) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleName(userId, roleName)
                .orElseThrow(() -> new RuntimeException("Role not found for user"));

        userRoleRepository.delete(userRole);
    }

    // Organization Management Methods

    public List<TenantOrganization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public void addUserToOrganization(Long userId, Long organizationId) {
        TenantUser user = tenantUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TenantOrganization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Check if already a member
        if (organizationMemberRepository.findByUserIdAndOrganizationId(userId, organizationId).isPresent()) {
            throw new RuntimeException("User is already a member of this organization");
        }

        OrganizationMember member = new OrganizationMember();
        member.setUserId(userId);
        member.setOrganizationId(organizationId);
        member.setRole("member");
        member.setJoinedAt(LocalDateTime.now());

        organizationMemberRepository.save(member);
    }

    public void removeUserFromOrganization(Long userId, Long organizationId) {
        OrganizationMember member = organizationMemberRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this organization"));

        organizationMemberRepository.delete(member);
    }

    // Dashboard Statistics

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = tenantUserRepository.count();
        long activeUsers = tenantUserRepository.countByStatus("active");
        long inactiveUsers = tenantUserRepository.countByStatus("inactive");
        long totalOrganizations = organizationRepository.count();
        long totalRoles = userRoleRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);
        stats.put("totalOrganizations", totalOrganizations);
        stats.put("totalRoles", totalRoles);
        stats.put("tenant", TenantContext.getCurrentTenant());

        return stats;
    }

    // Helper Methods

    private AdminUserDTO convertToAdminUserDTO(TenantUser user) {
        List<String> roles = userRoleRepository.findByUserId(user.getId())
                .stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.toList());

        List<String> organizations = organizationMemberRepository.findByUserId(user.getId())
                .stream()
                .map(member -> {
                    TenantOrganization org = organizationRepository.findById(member.getOrganizationId()).orElse(null);
                    return org != null ? org.getName() : null;
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());

        return new AdminUserDTO(
                user.getId(),
                user.getKeycloakUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getPhone(),
                user.getDepartment(),
                user.getPosition(),
                roles,
                organizations,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    private UserRoleDTO convertToUserRoleDTO(UserRole userRole) {
        return new UserRoleDTO(
                userRole.getId(),
                userRole.getRoleName(),
                getRoleDescription(userRole.getRoleName()),
                getRolePermissions(userRole.getRoleName()),
                userRole.getAssignedAt(),
                userRole.getAssignedBy()
        );
    }

    private String getRoleDescription(String roleName) {
        switch (roleName.toLowerCase()) {
            case "admin":
                return "Full administrative access to the system";
            case "user":
                return "Standard user access";
            case "manager":
                return "Manager level access with team management capabilities";
            default:
                return "Custom role";
        }
    }

    private String getRolePermissions(String roleName) {
        switch (roleName.toLowerCase()) {
            case "admin":
                return "all";
            case "user":
                return "read,write";
            case "manager":
                return "read,write,manage_team";
            default:
                return "read";
        }
    }
}
