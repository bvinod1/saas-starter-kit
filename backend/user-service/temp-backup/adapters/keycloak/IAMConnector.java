/*******************************************************************************
 *
 * Copyright (c) 2020-21 Mindsprint Limited
 *
 * All information contained herein is, and remains the property of Mindsprint
 * Limited. The intellectual and technical concepts contained herein are
 * proprietary to Mindsprint and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from Mindsprint
 * Limited
 *
 *******************************************************************************/
package com.saasstarterkit.user.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.saasstarterkit.user.dto.*;
import com.saasstarterkit.user.service.interfaces.IIAMAuthentication;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.saasstarterkit.user.config.IAMConstants;
import com.saasstarterkit.user.config.IAMPropertyReader;
import com.saasstarterkit.user.config.IAMUtils;
import com.saasstarterkit.user.exception.IAMConnectorException;
import com.saasstarterkit.user.security.CurrentUser;
import com.saasstarterkit.user.security.CurrentUserProvider;
import com.saasstarterkit.user.service.interfaces.IIAMConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasstarterkit.user.config.IAMAdminClientConfig;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import com.saasstarterkit.user.config.IAMAdminClientUtils;
import com.saasstarterkit.user.config.IAMAdminClientConfigBuilder;
import java.util.Collections;
import org.keycloak.OAuth2Constants;

/**
 * This connector service gives implementation of IIAMConnector interface
 * 
 * @author Mohankumar.D
 *
 */
@Service
class IAMConnector implements IIAMConnector {

	@Autowired
	IIAMAuthentication iiamAuthentication;

	private final CurrentUserProvider currentUserProvider;

	private final IAMUtils iamUtils;

	private final IAMPropertyReader keycloakPropertyReader;

	private static final String REFRESH_TOKEN = "refresh_token";

	private static final Logger log = LoggerFactory.getLogger(IAMConnector.class);

	final RestTemplate restTemplate;

	public IAMConnector(CurrentUserProvider currentUserProvider, IAMUtils iamUtils, IAMPropertyReader keycloakPropertyReader, RestTemplate restTemplate) {
		this.currentUserProvider = currentUserProvider;
		this.iamUtils = iamUtils;
		this.keycloakPropertyReader = keycloakPropertyReader;
		this.restTemplate = restTemplate;
	}

	/**
	 * This method is used to retrieve current logged-in users roles
	 * 
	 * @return Returns roles assigned to a logged-in user
	 */
	public List<String> getCurrentUserRoles() {
		return currentUserProvider.getCurrentUser().getRoles();
	}

	/**
	 * This method is used to retrieve current logged-in users information
	 * 
	 * @return Returns user profile of a logged-in user
	 */
	@Override
	public CurrentUser getCurrentUser() {
		return currentUserProvider.getCurrentUser();
	}

	/**
	 * This method is used to retrieve requested user information
	 * 
	 * @param userid -[Keycloak userid
	 * @return iamUserDTO -[MindsprintIAM user object
	 */
	@Override
	public IAMUserInfoDTO getIAMUser(String userid) {
		UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
				.get(userid);
		IAMUserInfoDTO iamUserInfoDTO;
		if (null != userResource) {
			UserRepresentation userRepresentation = userResource.toRepresentation();

			iamUserInfoDTO = new IAMUserInfoDTO();
			iamUserInfoDTO.setUserid(userRepresentation.getId());
			iamUserInfoDTO.setUsername(userRepresentation.getUsername());
			iamUserInfoDTO.setEmail(userRepresentation.getEmail());
			iamUserInfoDTO.setFirstName(userRepresentation.getFirstName());
			iamUserInfoDTO.setLastName(userRepresentation.getLastName());
			iamUserInfoDTO.setEnabled(userRepresentation.isEnabled());
			for (String requiredAction : userRepresentation.getRequiredActions()) {
				if (requiredAction.equals(IAMConstants.UPDATE_PASSWORD))
					iamUserInfoDTO.setIsTemporaryPassword(true);
			}
			iamUserInfoDTO.setRoles(userResource.roles().realmLevel().listEffective().stream()
					.map(RoleRepresentation::getName).collect(Collectors.toList()));
			iamUserInfoDTO.setAttributes(userRepresentation.getAttributes());

		} else {
			throw new IAMConnectorException(IAMConstants.USER_NOT_FOUND);
		}
		return iamUserInfoDTO;
	}

	/**
	 * This method is used to create a new user in Mindsprint IAM server
	 * 
	 * @param iamUserDTO -[MindsprintIAM user object
	 * @return -[return update MindsprintIAM user object
	 */
	public IAMUserDTO createIAMUser(IAMUserDTO iamUserDTO) {
		try {
			UserModel model = new UserModel();
			BeanUtils.copyProperties(iamUserDTO, model);

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(List.of(MediaType.APPLICATION_JSON));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", currentUserProvider.accessToken());

			HttpEntity<String> postEntity = new HttpEntity<>(new ObjectMapper().writeValueAsString(model), headers);
			HttpHeaders responseHeaders = new HttpHeaders();
			try {
				ResponseEntity<String> object = restTemplate.exchange(
						iamUtils.createUserEndpoint(keycloakPropertyReader), HttpMethod.POST, postEntity, String.class);
				iamUserDTO.setStatusCode(object.getStatusCode().value());
				iamUserDTO.setStatus(HttpStatus.resolve(object.getStatusCode().value()).getReasonPhrase());
				responseHeaders = object.getHeaders();
			} catch (HttpClientErrorException exception) {
				iamUserDTO.setStatusCode(exception.getStatusCode().value());
				iamUserDTO.setStatus(HttpStatus.resolve(exception.getStatusCode().value()).getReasonPhrase());
			}

			RealmResource realmResource = currentUserProvider.currentRealmResource(keycloakPropertyReader);
			UsersResource usersResource = realmResource.users();
			System.err.println("Newly Created user status code " + iamUserDTO.getStatusCode());
			if (iamUserDTO.getStatusCode() == 201) {
				String[] str = Objects.requireNonNull(responseHeaders.get("Location")).get(0).split("/");
				String userId = str[str.length - 1];
				iamUserDTO.setUserid(userId);
				System.err.println("Newly Created user ID " + userId);
				UserResource userResource = usersResource.get(userId);

				if (null != iamUserDTO.getRoles() && !iamUserDTO.getRoles().isEmpty()) {
					Boolean pass = updateRoles(RoleAction.Available, userResource, iamUserDTO.getRoles());
					System.err.println("Role Pass: " + pass);
				}

				if (null != iamUserDTO.getAttributes() && !iamUserDTO.getAttributes().isEmpty()) {
					Boolean att = updateAttributes(userResource, iamUserDTO.getAttributes());
					System.err.println("Attribute Pass: " + att);
				}

				// create password credential
				if (null != iamUserDTO.getPassword() && !iamUserDTO.getPassword().isBlank())
					resetPassword(userResource, iamUserDTO);

			}

		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return iamUserDTO;
	}

	/**
	 * To check user available in Keycloak user list
	 * 
	 * @param username -[username needs to check the availability
	 * @return -[ state of user present (true/false)
	 */
	public IAMUserExistDTO isUserExist(String username) {
		IAMUserExistDTO iamUserExistDTO = new IAMUserExistDTO();
		List<UserRepresentation> userRepresentations = currentUserProvider.currentRealmResource(keycloakPropertyReader)
				.users().search(username, true);
		if (!userRepresentations.isEmpty()) {
			iamUserExistDTO.setUserid(userRepresentations.get(0).getId());
			iamUserExistDTO.setStatus("true");
		} else {
			iamUserExistDTO.setStatus("false");
		}
		return iamUserExistDTO;
	}

	/**
	 * This method used to retrieve roles available in MindsprintIAM
	 * 
	 * @return -[MindsprintIAM Role list
	 */
	public List<String> getIAMRoles() {
		RealmResource realmResource = currentUserProvider.currentRealmResource(keycloakPropertyReader);
		return realmResource.roles().list().stream().map(RoleRepresentation::getName).collect(Collectors.toList());
	}

	/**
	 * This method used to retrieve roles assigned user
	 * 
	 * @return -[Mindsprint User IAM Role list
	 */
	public List<String> getIAMUserRoles(String userId) {
		UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
				.get(userId);
		return userResource.roles().realmLevel().listEffective().stream().map(RoleRepresentation::getName)
				.collect(Collectors.toList());
	}

	/**
	 * This method to retrieve all users attributes
	 * 
	 * @param userId -[User id needs to retrieve attribute
	 * @return -[User's Attributes Map<String, List<String>>
	 */
	public Map<String, List<String>> getUserAttributes(String userId) {
		try {
			UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
					.get(userId);
			UserRepresentation userRepresentation = userResource.toRepresentation();
			return userRepresentation.getAttributes();
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
	}

	/**
	 * To update user attributes (create new / update existing)
	 * 
	 * @param userId         -[User id needs to retrieve attribute
	 * @param userAttributes -[User attribute object
	 * @return -[ status of update
	 */
	public Boolean updateAttributes(String userId, Map<String, List<String>> userAttributes) {
		UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
				.get(userId);
		return updateAttributes(userResource, userAttributes);
	}

	/**
	 * Remove users attribute
	 * 
	 * @param userId       -[User id needs to retrieve attribute
	 * @param attributeKey -[User attribute key
	 * @return -[ status of update
	 */
	public Boolean removeAttribute(String userId, String attributeKey) {
		UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
				.get(userId);
		UserRepresentation userRepresentation = userResource.toRepresentation();
		try {
			Map<String, List<String>> attributes = userRepresentation.getAttributes();
			attributes.remove(attributeKey);
			userRepresentation.setAttributes(attributes);
			userResource.update(userRepresentation);
			return true;
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
	}

	/**
	 * This method used to update user roles (assign/remove) to a specific user in
	 * MindsprintIAM
	 * 
	 * @param roleAction -[Effective(remove roles)/Available(add roles) based on
	 *                   needs
	 * @param userId     -[User id needs to update roles
	 * @param roles      -[Roles which are available or assigned
	 */
	public Boolean updateRoles(RoleAction roleAction, String userId, String... roles) {
		Boolean returnValue = false;
		try {
			UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
					.get(userId);
			updateRoles(roleAction, userResource, Arrays.asList(roles));
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return returnValue;
	}

	/**
	 * This method used to reset password and update temporary flag
	 * 
	 * @param iamUserDTO    -[MindsprintIAM user Object updated password and temporary
	 *                      flag
	 * @param isCurrentUser -[ user type
	 * @return -[return update MindsprintIAM user object
	 */
	public IAMUserDTO resetPassword(IAMUserDTO iamUserDTO, Boolean isCurrentUser) {
		try {
			String userid;
			if (!isCurrentUser) {
				if (null == iamUserDTO.getUserid() && iamUserDTO.getUserid().isBlank())
					throw new IAMConnectorException("User ID missing");
				else
					userid = iamUserDTO.getUserid();
			} else {
				userid = currentUserProvider.getCurrentUser().getUserId();
			}
			UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
					.get(userid);
			if (null != iamUserDTO.getOldPassword()) {
				validatePassword(userResource.toRepresentation().getUsername(), iamUserDTO.getOldPassword());
			}
			resetPassword(userResource, iamUserDTO);
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return iamUserDTO;
	}

	/**
	 * This method used to update First / Last name
	 * 
	 * @param iamUserDTO -[MindsprintIAM user Object updated password and temporary flag
	 * @return -[return update MindsprintIAM user object
	 */
	public IAMUpdateUserDTO updateIAMUser(IAMUpdateUserDTO iamUserDTO) {
		try {
			RealmResource realmResource = currentUserProvider.currentRealmResource(keycloakPropertyReader);
			UserResource userResource = realmResource.users().get(iamUserDTO.getUserid());
			UserRepresentation representation = userResource.toRepresentation();
			if(null != iamUserDTO.getFirstname() && !iamUserDTO.getFirstname().isBlank())
				representation.setFirstName(iamUserDTO.getFirstname());
			if(null != iamUserDTO.getLastname() && !iamUserDTO.getLastname().isBlank())
				representation.setLastName(iamUserDTO.getLastname());
			if(null != iamUserDTO.getEmail() && !iamUserDTO.getEmail().isBlank())
				representation.setEmail(iamUserDTO.getEmail());
			userResource.update(representation);
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return iamUserDTO;
	}

	/**
	 * This method used to enable/disable a user in MindsprintIAM, disabled user cannot
	 * log in into the system
	 * 
	 * @param userState -[State Enable/Disable of the user need to update
	 * @param userid    -[MindsprintIAM user's id
	 * @return boolean -[return true on success
	 */
	public Boolean updateIAMUserState(UserState userState, String userid) {
		try {

			RealmResource realmResource = currentUserProvider.currentRealmResource(keycloakPropertyReader);
			UserResource userResource = realmResource.users().get(userid);
			UserRepresentation representation = userResource.toRepresentation();
			log.error("Info: State of the user {} - {} is changed to {}", userid, representation.getUsername(), userState);
			representation.setEnabled(UserState.Enable == userState);
			userResource.update(representation);
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return true;
	}

	/**
	 * This method will give access to get current user's user attributes
	 * 
	 * @return Map<String, Object> Collection of user attribute
	 */
	public Map<String, Object> getCurrentUserAttributes() {
		Map<String, Object> accessTokenClaims = currentUserProvider.getCurrentUserPrincipal().getAttributes();
		return accessTokenClaims;
	}

	/**
	 * This method will give access to add realm role
	 * 
	 * @param role as String
	 * @return response as boolean state
	 */
	public Boolean addRole(String role) {
		try {
			RealmResource realmResource = currentUserProvider.currentRealmResource(keycloakPropertyReader);
			RoleRepresentation roleRepresentation = new RoleRepresentation();
			roleRepresentation.setClientRole(false);
			roleRepresentation.setName(role);
			realmResource.roles().create(roleRepresentation);
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return true;
	}

	/**
	 * This method will give access to remove realm role
	 * 
	 * @param role as String
	 * @return response as boolean state
	 */
	public Boolean removeRole(String role) {
		try {
			RealmResource realmResource = currentUserProvider.currentRealmResource(keycloakPropertyReader);
			realmResource.roles().deleteRole(role);
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return true;
	}
	
	/**
	 * This method helps to update username/email and remove user IDP mapped
	 * 
	 * @param users - list of IDP user detail
	 * @return response as boolean state
	 */
	public Boolean updateIDPUserName(List<IAMIDPUserUpdate> users) {
		RealmResource realmResource = currentUserProvider.currentRealmResource(keycloakPropertyReader);
		RealmRepresentation realmRepresentation = realmResource.toRepresentation();
		String idpAlias = realmRepresentation.getIdentityProviders().get(0).getAlias();
		
		if(realmRepresentation.isEditUsernameAllowed()) {
			users.parallelStream().forEach((iamIDPUserUpdate) -> {
				final UserResource userResource = realmResource.users().get(iamIDPUserUpdate.getUserid());
				if(userResource.getFederatedIdentity().size() > 0) 
					userResource.removeFederatedIdentity(idpAlias);
				final UserRepresentation representation = userResource.toRepresentation();
				representation.setUsername(iamIDPUserUpdate.getNewemail());
				representation.setEmail(iamIDPUserUpdate.getNewemail());
				userResource.update(representation);
			});
		} else {
			throw new IAMConnectorException("Edit username setting is not enabled");
		}
		return true;
	}
	
	/**
	 * Search for users whose username or email matches the value provided by search
	 * 
	 * @param filter - the value to search. It can be the username, email
	 * @param firstIndex-index - the position of the first result to retrieve
	 * @param maxResult - the maximum number of results to retrieve
	 */
	public List<UserModel> getUsers(String filter, int firstIndex, int maxResult) {
		List<UserModel> userModels = new ArrayList<>();
		try {
			List<UserRepresentation> userRepresentations = currentUserProvider.currentRealmResource(keycloakPropertyReader)
					.users().search(filter, firstIndex, maxResult, true);
			userRepresentations.forEach(cm -> userModels.add(new UserModel(cm.getEmail(), cm.getFirstName(), cm.getLastName(), cm.getUsername(), cm.isEnabled())));
			
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return userModels;
	}

	private List<RoleRepresentation> getRoleRepresentation(RoleAction roleAction, UserResource userResource) {
		return roleAction == RoleAction.Available ? userResource.roles().realmLevel().listAvailable()
				: userResource.roles().realmLevel().listEffective();
	}

	private Boolean rolesUpdate(RoleAction roleAction, UserResource userResource,
			List<RoleRepresentation> roleRepresentationList) {
		try {
			if (roleAction == RoleAction.Available)
				userResource.roles().realmLevel().add(roleRepresentationList);
			else
				userResource.roles().realmLevel().remove(roleRepresentationList);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private Boolean updateRoles(RoleAction roleAction, UserResource userResource, List<String> roles) {
		Boolean returnValue = false;
		try {
			List<RoleRepresentation> roleRepresentationList = new ArrayList<>();
			for (String reqRole : roles) {
				getRoleRepresentation(roleAction, userResource).stream()
						.filter(role -> role.getName().equals(reqRole)).findAny().ifPresent(roleRepresentationList::add);

			}
			if (!roleRepresentationList.isEmpty()) {
				returnValue = rolesUpdate(roleAction, userResource, roleRepresentationList);
			}
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
		return returnValue;
	}

	private void resetPassword(UserResource userResource, IAMUserDTO iamUserDTO) {
		try {
			CredentialRepresentation newCredential = new CredentialRepresentation();
			newCredential.setType(CredentialRepresentation.PASSWORD);
			newCredential.setValue(iamUserDTO.getPassword());
			newCredential.setTemporary(iamUserDTO.getIsTemporaryPassword());
			userResource.resetPassword(newCredential);
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
	}

	private void validatePassword(String userName, String password) {
		try {
			UserCredentials userCredentials = new UserCredentials();
			userCredentials.setUsername(userName);
			userCredentials.setPassword(password);
			String token = iiamAuthentication.getUserAuthToken(userCredentials, "pwd",
					IIAMAuthentication.GrantType.password, IIAMAuthentication.Scope.none);
			JsonObject tokenJson = new Gson().fromJson(token, JsonObject.class);
			if (tokenJson == null || !tokenJson.has(REFRESH_TOKEN)) {
				throw new IAMConnectorException("Invalid credentials for the user");
			}
			iiamAuthentication.logoutUserSession(tokenJson.get(REFRESH_TOKEN).getAsString(),
					"pwd", IIAMAuthentication.GrantType.logout);
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
	}

	private Boolean updateAttributes(UserResource userResource, Map<String, List<String>> newAttributes) {
		try {
			UserRepresentation userRepresentation = userResource.toRepresentation();
			Map<String, List<String>> availableAttributes = userRepresentation.getAttributes();
			Map<String, List<String>> concatAttributes = null;
			if (null != availableAttributes) {
				concatAttributes = Stream
						.concat(availableAttributes.entrySet().stream(), newAttributes.entrySet().stream())
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2,
								HashMap::new));
			}

			userRepresentation.setAttributes(concatAttributes != null ? concatAttributes : newAttributes);
			userResource.update(userRepresentation);
			return true;
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
	}
	
	/**
	 * Perform mail action.
	 *
	 * @param userId - KC user id
	 * @param emailActions - emailActions
	 * @return string - Success message
	 */
	@Override
	public String triggerEmail(String userId, List<MailAction> emailActions) {
		try {
			if (userId != null && emailActions != null && !emailActions.isEmpty()) {
				List<String> actions = emailActions.stream().map(MailAction::name).collect(Collectors.toList());
				UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
						.get(userId);
				userResource.executeActionsEmail(keycloakPropertyReader.getProperty(IAMConstants.NOTIFICATION_CLIENTID),
						keycloakPropertyReader.getProperty(IAMConstants.NOTIFICATION_REDIRECT_URI),
						Integer.valueOf(keycloakPropertyReader.getProperty(IAMConstants.KNOTIFICATION_LIFESPAN)), actions);
			}
			return "Email sent successfully";
		} catch (Exception e) {
			throw new IAMConnectorException(e.getMessage());
		}
	}

	@Override
	public int logoutAllUserSessions(String userId) {
		UserResource userResource = currentUserProvider.currentRealmResource(keycloakPropertyReader).users()
				.get(userId);
		// Get all user sessions
		List<UserSessionRepresentation> userSessions = userResource.getUserSessions();
		int sessionCount = userSessions.size();
		// Logout all sessions
		userResource.logout();
		return sessionCount;
	}
	/**
	 * Loads the keycloak configuration from system property.
	 * 
	 * @param keycloakPropertyReader contains keycloak property details
	 * @return keycloak configuration
	 * @see IAMAdminClientConfig
	 */
	//@param keycloakPropertyReader Read parameters from the application properties file
	public static IAMAdminClientConfig loadConfig(IAMPropertyReader keycloakPropertyReader) {

		IAMAdminClientConfigBuilder builder = IAMAdminClientConfig.builder();

		try {
			builder = builder.serverUrl(keycloakPropertyReader.getProperty("keycloak.auth-server-url"));
			builder = builder.realm(keycloakPropertyReader.getProperty("keycloak.realm"));
			builder = builder.clientId(keycloakPropertyReader.getProperty("keycloak.resource"));
			builder = builder.clientSecret(keycloakPropertyReader.getProperty("keycloak.credentials.secret"));
		} catch (Exception e) {
			log.error("Error: Loading keycloak admin configuration => {}", e.getMessage());
		}

		IAMAdminClientConfig config = builder.build();
		log.debug("Found keycloak configuration: {}", config);

		return config;
	}
	/**
	 * It builds a {@link Keycloak} client from a given configuration. This client
	 * is used to communicate with the Keycloak instance via REST API.
	 * 
	 * @param session the security context
	 * @param config  keycloak configuration
	 * @return Keycloak instance
	 * @see Keycloak
	 * @see IAMAdminClientConfig
	 */
	public static Keycloak getClient(String token, IAMAdminClientConfig config) {

		return KeycloakBuilder.builder() //
				.serverUrl(config.getServerUrl()) //
				.realm(config.getRealm()) //
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
				.clientId(config.getClientId()) //
				.clientSecret(config.getClientSecret()) //
				.authorization(token) //
				.build();
	}
	/**
	 * Adds a role to a composite role. A composite role is just a role that
	 * contains sub roles.
	 * 
	 * @param keycloak                  keycloak instance
	 * @param keycloakAdminClientConfig keycloak configuration
	 * @param client                    client id
	 * @param role                      role to be added
	 * @param compositeRole             where the role will be added
	 */
	public static void addRoleToListOf(Keycloak keycloak, IAMAdminClientConfig keycloakAdminClientConfig, String client,
			String role, String compositeRole) {

		final String clientUuid = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().findByClientId(client)
				.get(0).getId();

		RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients().get(clientUuid)
				.roles();

		final List<RoleRepresentation> existingRoles = rolesResource.list();

		final boolean roleExists = existingRoles.stream().anyMatch(r -> r.getName().equals(role));

		if (!roleExists) {
			RoleRepresentation roleRepresentation = new RoleRepresentation();
			roleRepresentation.setName(role);
			roleRepresentation.setClientRole(true);
			roleRepresentation.setComposite(false);

			rolesResource.create(roleRepresentation);
		}

		final boolean compositeExists = existingRoles.stream().anyMatch(r -> r.getName().equals(compositeRole));

		if (!compositeExists) {
			RoleRepresentation compositeRoleRepresentation = new RoleRepresentation();
			compositeRoleRepresentation.setName(compositeRole);
			compositeRoleRepresentation.setClientRole(true);
			compositeRoleRepresentation.setComposite(true);

			rolesResource.create(compositeRoleRepresentation);
		}

		final RoleResource compositeRoleResource = rolesResource.get(compositeRole);

		final boolean alreadyAdded = compositeRoleResource.getRoleComposites().stream()
				.anyMatch(r -> r.getName().equals(role));

		if (!alreadyAdded) {
			final RoleRepresentation roleToAdd = rolesResource.get(role).toRepresentation();
			compositeRoleResource.addComposites(Collections.singletonList(roleToAdd));
		}
	}

	/**
	 * Removes a given role from a composite role.
	 * 
	 * @param keycloak                  keycloak instance
	 * @param keycloakAdminClientConfig keycloak configuration
	 * @param role                      role to be deleted
	 * @param compositeRole             where the role should be deleted
	 */
	public static void removeRoleInListOf(Keycloak keycloak, IAMAdminClientConfig keycloakAdminClientConfig,
			String role, String compositeRole) {

		final String clientUuid = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients()
				.findByClientId(keycloakAdminClientConfig.getClientId()).get(0).getId();

		final RolesResource rolesResource = keycloak.realm(keycloakAdminClientConfig.getRealm()).clients()
				.get(clientUuid).roles();

		final RoleResource compositeRoleResource = rolesResource.get(compositeRole);

		try {
			final RoleRepresentation roleToDelete = rolesResource.get(role).toRepresentation();
			compositeRoleResource.getRoleComposites().remove(roleToDelete);

		} catch (Exception e) {
			log.warn("Role {} does not exists!", role);
		}
	}

	/**
	 * Removes a role from a given list of roles.
	 * 
	 * @param listOfRoleRepresentation list of roles
	 * @param roleToBeRemove           role to be remove from the list
	 * @return Returns updated roles (List)
	 */
	public static List<RoleRepresentation> removeRoleInList(List<RoleRepresentation> listOfRoleRepresentation,
			RoleRepresentation roleToBeRemove) {

		listOfRoleRepresentation.remove(roleToBeRemove);

		List<RoleRepresentation> updatedListRoleRepresentation = new ArrayList<>();
		for (RoleRepresentation roleRepresentationItem : listOfRoleRepresentation) {
			if (!roleToBeRemove.getName().equalsIgnoreCase(roleRepresentationItem.getName())) {
				updatedListRoleRepresentation.add(roleRepresentationItem);
			}
		}

		return updatedListRoleRepresentation;
	}
	public String createUserEndpoint(IAMPropertyReader keycloakPropertyReader) {
		return keycloakPropertyReader.getProperty("keycloak.auth-server-url") + "/admin/realms/"
				+ keycloakPropertyReader.getProperty("keycloak.realm") + "/users";
	}
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST)
	@Override
	public CurrentUser getCurrentUserFromPrincipal() {

		CurrentUser result = new CurrentUser();

		OAuth2AuthenticatedPrincipal principal = currentUserProvider.getCurrentUserPrincipal();

		String userId = principal.getName();
		String username = principal.getAttribute("preferred_username");
		Map<String, List<String>> roles = principal.getAttribute("realm_access");

		if (roles != null) {
			List<String> realmRoles = roles.get("roles");
			List<String> transformedRoles = (realmRoles != null)
					? realmRoles.stream().filter(role -> role != null).filter(roleName -> roleName != null)
							.map(roleName -> "ROLE_" + roleName.toUpperCase()).collect(Collectors.toList())
					: List.of();
			result.setRoles(transformedRoles);
		}
		result.setUserId(userId);
		result.setUsername(username);
		result.setEmail(principal.getAttribute("email"));

		return result;
	}

	public RealmResource currentRealmResource(IAMPropertyReader keycloakPropertyReader) {
		IAMAdminClientConfig keycloakAdminClientConfig = IAMAdminClientUtils.loadConfig(keycloakPropertyReader);
		Keycloak keycloak = IAMAdminClientUtils
				.getKeycloakClient(currentUserProvider.accessToken(), keycloakAdminClientConfig);

		// Get realm
		RealmResource realmResource = keycloak.realm(keycloakAdminClientConfig.getRealm());

		return realmResource;
	}

}