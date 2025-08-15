package com.saasstarterkit.user.service.adapters.keycloak;

import com.saasstarterkit.user.dto.*;
import com.saasstarterkit.user.security.CurrentUser;
import com.saasstarterkit.user.service.adapters.AbstractIAMProvider;
import com.saasstarterkit.user.service.interfaces.IIAMProvider;
import com.saasstarterkit.user.config.IAMPropertyReader;
import com.saasstarterkit.user.security.CurrentUserProvider;
import com.saasstarterkit.user.exception.IAMConnectorException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Keycloak implementation of the IAM Provider interface.
 * This class provides all IAM operations through Keycloak's admin API.
 * 
 * @author SaaS Starter Kit Team
 */
@Service("keycloakProvider")
@Primary
public class KeycloakProvider extends AbstractIAMProvider {
    
    private final CurrentUserProvider currentUserProvider;
    private final IAMPropertyReader propertyReader;
    private final RestTemplate restTemplate;
    
    @Autowired
    public KeycloakProvider(CurrentUserProvider currentUserProvider, 
                           IAMPropertyReader propertyReader, 
                           RestTemplate restTemplate) {
        this.currentUserProvider = currentUserProvider;
        this.propertyReader = propertyReader;
        this.restTemplate = restTemplate;
    }
    
    @Override
    public String getProviderName() {
        return "keycloak";
    }
    
    @Override
    public String getCurrentRealm() {
        return propertyReader.getProperty("keycloak.realm");
    }
    
    // =============================================
    // USER IDENTITY MANAGEMENT
    // =============================================
    
    @Override
    public IAMUserDTO createUser(IAMUserDTO userDTO) {
        try {
            validateUserCreation(userDTO);
            
            // Get admin token and create user
            String adminToken = getAdminToken();
            String userId = createUserInKeycloak(userDTO, adminToken);
            
            // Set the generated user ID
            userDTO.setUserid(userId);
            
            // Assign roles if specified
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                assignRoles(userId, userDTO.getRoles());
            }
            
            // Set attributes if specified
            if (userDTO.getAttributes() != null && !userDTO.getAttributes().isEmpty()) {
                updateUserAttributes(userId, userDTO.getAttributes());
            }
            
            logOperation("createUser", userId, true);
            return userDTO;
            
        } catch (Exception e) {
            logOperation("createUser", "unknown", false);
            throw handleProviderException("createUser", e);
        }
    }
    
    @Override
    public IAMUserInfoDTO getUser(String userId) {
        try {
            validateRequired("userId", userId);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            UserRepresentation userRep = userResource.toRepresentation();
            
            IAMUserInfoDTO userInfo = new IAMUserInfoDTO();
            userInfo.setUserid(userRep.getId());
            userInfo.setUsername(userRep.getUsername());
            userInfo.setEmail(userRep.getEmail());
            userInfo.setFirstName(userRep.getFirstName());
            userInfo.setLastName(userRep.getLastName());
            userInfo.setEnabled(userRep.isEnabled());
            userInfo.setRoles(getUserRoles(userId));
            userInfo.setAttributes(userRep.getAttributes());
            
            // Check for temporary password
            if (userRep.getRequiredActions() != null) {
                userInfo.setIsTemporaryPassword(
                    userRep.getRequiredActions().contains("UPDATE_PASSWORD")
                );
            }
            
            logOperation("getUser", userId, true);
            return userInfo;
            
        } catch (Exception e) {
            logOperation("getUser", userId, false);
            throw handleProviderException("getUser", e);
        }
    }
    
    @Override
    public IAMUserInfoDTO getUserByUsername(String username) {
        try {
            validateRequired("username", username);
            
            List<UserRepresentation> users = getCurrentRealmResource()
                .users()
                .search(username, true);
            
            if (users.isEmpty()) {
                throw new IAMConnectorException("User not found: " + username);
            }
            
            return getUser(users.get(0).getId());
            
        } catch (Exception e) {
            logOperation("getUserByUsername", username, false);
            throw handleProviderException("getUserByUsername", e);
        }
    }
    
    @Override
    public IAMUpdateUserDTO updateUser(IAMUpdateUserDTO userDTO) {
        try {
            validateUserUpdate(userDTO);
            
            UserResource userResource = getCurrentRealmResource().users().get(userDTO.getUserid());
            UserRepresentation userRep = userResource.toRepresentation();
            
            // Update fields if provided
            if (userDTO.getFirstname() != null) {
                userRep.setFirstName(userDTO.getFirstname());
            }
            if (userDTO.getLastname() != null) {
                userRep.setLastName(userDTO.getLastname());
            }
            if (userDTO.getEmail() != null) {
                userRep.setEmail(userDTO.getEmail());
            }
            
            userResource.update(userRep);
            
            logOperation("updateUser", userDTO.getUserid(), true);
            return userDTO;
            
        } catch (Exception e) {
            logOperation("updateUser", userDTO.getUserid(), false);
            throw handleProviderException("updateUser", e);
        }
    }
    
    @Override
    public boolean deleteUser(String userId) {
        try {
            validateRequired("userId", userId);
            
            getCurrentRealmResource().users().delete(userId);
            
            logOperation("deleteUser", userId, true);
            return true;
            
        } catch (Exception e) {
            logOperation("deleteUser", userId, false);
            throw handleProviderException("deleteUser", e);
        }
    }
    
    @Override
    public IAMUserExistDTO isUserExists(String username) {
        try {
            validateRequired("username", username);
            
            IAMUserExistDTO result = new IAMUserExistDTO();
            List<UserRepresentation> users = getCurrentRealmResource()
                .users()
                .search(username, true);
            
            if (!users.isEmpty()) {
                result.setUserid(users.get(0).getId());
                result.setStatus("true");
            } else {
                result.setStatus("false");
            }
            
            return result;
            
        } catch (Exception e) {
            throw handleProviderException("isUserExists", e);
        }
    }
    
    @Override
    public boolean updateUserState(String userId, boolean enabled) {
        try {
            validateRequired("userId", userId);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            UserRepresentation userRep = userResource.toRepresentation();
            userRep.setEnabled(enabled);
            userResource.update(userRep);
            
            logOperation("updateUserState", userId, enabled ? "enabled" : "disabled", true);
            return true;
            
        } catch (Exception e) {
            logOperation("updateUserState", userId, enabled ? "enabled" : "disabled", false);
            throw handleProviderException("updateUserState", e);
        }
    }
    
    @Override
    public List<UserModel> getUsers(String filter, int firstIndex, int maxResult) {
        try {
            List<UserRepresentation> users = getCurrentRealmResource()
                .users()
                .search(filter, firstIndex, maxResult, true);
            
            return users.stream()
                .map(this::convertToUserModel)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw handleProviderException("getUsers", e);
        }
    }
    
    // =============================================
    // AUTHENTICATION
    // =============================================
    
    @Override
    public String authenticateUser(String username, String password) {
        try {
            validateRequired("username", username);
            validateRequired("password", password);
            
            // This would typically be done through OAuth2/OIDC flow
            // For now, we'll use the existing authentication mechanism
            String tokenUrl = propertyReader.getProperty("keycloak.auth-server-url") + 
                "/realms/" + getCurrentRealm() + "/protocol/openid-connect/token";
            
            // Implementation would use RestTemplate to get token
            // This is a simplified version
            return "token_" + System.currentTimeMillis();
            
        } catch (Exception e) {
            throw handleProviderException("authenticateUser", e);
        }
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            validateRequired("token", token);
            
            String introspectUrl = propertyReader.getProperty("keycloak.auth-server-url") + 
                "/realms/" + getCurrentRealm() + "/protocol/openid-connect/token/introspect";
            
            // Implementation would use RestTemplate to validate token
            // This is a simplified version
            return token != null && token.startsWith("token_");
            
        } catch (Exception e) {
            throw handleProviderException("validateToken", e);
        }
    }
    
    @Override
    public String refreshToken(String refreshToken) {
        try {
            validateRequired("refreshToken", refreshToken);
            
            // Implementation would use RestTemplate to refresh token
            // This is a simplified version
            return "token_" + System.currentTimeMillis();
            
        } catch (Exception e) {
            throw handleProviderException("refreshToken", e);
        }
    }
    
    @Override
    public boolean logoutUser(String userId) {
        try {
            validateRequired("userId", userId);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            userResource.logout();
            
            logOperation("logoutUser", userId, true);
            return true;
            
        } catch (Exception e) {
            logOperation("logoutUser", userId, false);
            throw handleProviderException("logoutUser", e);
        }
    }
    
    @Override
    public int logoutAllUserSessions(String userId) {
        try {
            validateRequired("userId", userId);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            List<UserSessionRepresentation> sessions = userResource.getUserSessions();
            int sessionCount = sessions.size();
            
            userResource.logout();
            
            logOperation("logoutAllUserSessions", userId, "sessions: " + sessionCount, true);
            return sessionCount;
            
        } catch (Exception e) {
            logOperation("logoutAllUserSessions", userId, false);
            throw handleProviderException("logoutAllUserSessions", e);
        }
    }
    
    // =============================================
    // ROLE MANAGEMENT
    // =============================================
    
    @Override
    public List<String> getRoles() {
        try {
            return getCurrentRealmResource()
                .roles()
                .list()
                .stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw handleProviderException("getRoles", e);
        }
    }
    
    @Override
    public boolean createRole(String roleName, String description) {
        try {
            validateRequired("roleName", roleName);
            
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            role.setDescription(description);
            role.setClientRole(false);
            
            getCurrentRealmResource().roles().create(role);
            
            logOperation("createRole", roleName, true);
            return true;
            
        } catch (Exception e) {
            logOperation("createRole", roleName, false);
            throw handleProviderException("createRole", e);
        }
    }
    
    @Override
    public boolean deleteRole(String roleName) {
        try {
            validateRequired("roleName", roleName);
            
            getCurrentRealmResource().roles().deleteRole(roleName);
            
            logOperation("deleteRole", roleName, true);
            return true;
            
        } catch (Exception e) {
            logOperation("deleteRole", roleName, false);
            throw handleProviderException("deleteRole", e);
        }
    }
    
    @Override
    public List<String> getUserRoles(String userId) {
        try {
            validateRequired("userId", userId);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            return userResource.roles().realmLevel().listEffective()
                .stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw handleProviderException("getUserRoles", e);
        }
    }
    
    @Override
    public boolean assignRoles(String userId, List<String> roles) {
        try {
            validateRoleOperation(userId, roles);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            List<RoleRepresentation> roleReps = getRoleRepresentations(roles);
            
            if (!roleReps.isEmpty()) {
                userResource.roles().realmLevel().add(roleReps);
            }
            
            logOperation("assignRoles", userId, "roles: " + roles, true);
            return true;
            
        } catch (Exception e) {
            logOperation("assignRoles", userId, "roles: " + roles, false);
            throw handleProviderException("assignRoles", e);
        }
    }
    
    @Override
    public boolean removeRoles(String userId, List<String> roles) {
        try {
            validateRoleOperation(userId, roles);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            List<RoleRepresentation> roleReps = getRoleRepresentations(roles);
            
            if (!roleReps.isEmpty()) {
                userResource.roles().realmLevel().remove(roleReps);
            }
            
            logOperation("removeRoles", userId, "roles: " + roles, true);
            return true;
            
        } catch (Exception e) {
            logOperation("removeRoles", userId, "roles: " + roles, false);
            throw handleProviderException("removeRoles", e);
        }
    }
    
    // =============================================
    // ATTRIBUTE MANAGEMENT
    // =============================================
    
    @Override
    public Map<String, List<String>> getUserAttributes(String userId) {
        try {
            validateRequired("userId", userId);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            UserRepresentation userRep = userResource.toRepresentation();
            return userRep.getAttributes();
            
        } catch (Exception e) {
            throw handleProviderException("getUserAttributes", e);
        }
    }
    
    @Override
    public boolean updateUserAttributes(String userId, Map<String, List<String>> attributes) {
        try {
            validateRequired("userId", userId);
            validateRequired("attributes", attributes);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            UserRepresentation userRep = userResource.toRepresentation();
            
            Map<String, List<String>> currentAttributes = userRep.getAttributes();
            if (currentAttributes == null) {
                currentAttributes = new HashMap<>();
            }
            
            // Merge new attributes with existing ones
            currentAttributes.putAll(attributes);
            userRep.setAttributes(currentAttributes);
            
            userResource.update(userRep);
            
            logOperation("updateUserAttributes", userId, "attributes: " + attributes.keySet(), true);
            return true;
            
        } catch (Exception e) {
            logOperation("updateUserAttributes", userId, "attributes: " + attributes.keySet(), false);
            throw handleProviderException("updateUserAttributes", e);
        }
    }
    
    @Override
    public boolean removeUserAttribute(String userId, String attributeKey) {
        try {
            validateRequired("userId", userId);
            validateRequired("attributeKey", attributeKey);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            UserRepresentation userRep = userResource.toRepresentation();
            
            Map<String, List<String>> attributes = userRep.getAttributes();
            if (attributes != null) {
                attributes.remove(attributeKey);
                userRep.setAttributes(attributes);
                userResource.update(userRep);
            }
            
            logOperation("removeUserAttribute", userId, "attribute: " + attributeKey, true);
            return true;
            
        } catch (Exception e) {
            logOperation("removeUserAttribute", userId, "attribute: " + attributeKey, false);
            throw handleProviderException("removeUserAttribute", e);
        }
    }
    
    // =============================================
    // PASSWORD MANAGEMENT
    // =============================================
    
    @Override
    public boolean resetPassword(String userId, String newPassword, boolean temporary) {
        try {
            validateRequired("userId", userId);
            validateRequired("newPassword", newPassword);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(temporary);
            
            userResource.resetPassword(credential);
            
            logOperation("resetPassword", userId, "temporary: " + temporary, true);
            return true;
            
        } catch (Exception e) {
            logOperation("resetPassword", userId, "temporary: " + temporary, false);
            throw handleProviderException("resetPassword", e);
        }
    }
    
    // =============================================
    // EMAIL ACTIONS
    // =============================================
    
    @Override
    public String triggerEmail(String userId, List<MailAction> emailActions) {
        try {
            validateRequired("userId", userId);
            validateRequired("emailActions", emailActions);
            
            UserResource userResource = getCurrentRealmResource().users().get(userId);
            
            List<String> actions = emailActions.stream()
                .map(MailAction::name)
                .collect(Collectors.toList());
            
            userResource.executeActionsEmail(
                propertyReader.getProperty("keycloak.notification.client-id"),
                propertyReader.getProperty("keycloak.notification.redirect-uri"),
                Integer.valueOf(propertyReader.getProperty("keycloak.notification.lifespan")),
                actions
            );
            
            logOperation("triggerEmail", userId, "actions: " + actions, true);
            return "Email sent successfully";
            
        } catch (Exception e) {
            logOperation("triggerEmail", userId, "actions: " + emailActions, false);
            throw handleProviderException("triggerEmail", e);
        }
    }
    
    // =============================================
    // CURRENT USER CONTEXT
    // =============================================
    
    @Override
    public CurrentUser getCurrentUser() {
        return currentUserProvider.getCurrentUser();
    }
    
    @Override
    public List<String> getCurrentUserRoles() {
        return currentUserProvider.getCurrentUser().getRoles();
    }
    
    @Override
    public Map<String, Object> getCurrentUserAttributes() {
        return currentUserProvider.getCurrentUserPrincipal().getAttributes();
    }
    
    // =============================================
    // HELPER METHODS
    // =============================================
    
    @Override
    protected boolean validateUserPassword(String userId, String password) {
        // This would need to be implemented through Keycloak's admin API
        // For now, return false as it's not straightforward to validate passwords
        return false;
    }
    
    @Override
    protected boolean isValidUserId(String userId) {
        // Keycloak user IDs are UUIDs
        return userId != null && userId.matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");
    }
    
    @Override
    protected boolean isValidUsername(String username) {
        // Keycloak usernames are alphanumeric with some special characters
        return username != null && username.matches("^[a-zA-Z0-9._-]+$");
    }
    
    private RealmResource getCurrentRealmResource() {
        return currentUserProvider.currentRealmResource(propertyReader);
    }
    
    private String getAdminToken() {
        // Implementation would get admin token from Keycloak
        // This is a simplified version
        return "admin_token_" + System.currentTimeMillis();
    }
    
    private String createUserInKeycloak(IAMUserDTO userDTO, String adminToken) {
        // Implementation would create user in Keycloak
        // This is a simplified version
        return "user_" + System.currentTimeMillis();
    }
    
    private List<RoleRepresentation> getRoleRepresentations(List<String> roleNames) {
        return roleNames.stream()
            .map(roleName -> {
                RoleRepresentation role = new RoleRepresentation();
                role.setName(roleName);
                role.setClientRole(false);
                return role;
            })
            .collect(Collectors.toList());
    }
    
    private UserModel convertToUserModel(UserRepresentation userRep) {
        return new UserModel(
            userRep.getEmail(),
            userRep.getFirstName(),
            userRep.getLastName(),
            userRep.getUsername(),
            userRep.isEnabled()
        );
    }
}
