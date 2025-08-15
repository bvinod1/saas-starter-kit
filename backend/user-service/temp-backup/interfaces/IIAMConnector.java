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
package com.saasstarterkit.user.service.interfaces;

import java.util.List;
import java.util.Map;

import com.saasstarterkit.user.dto.IAMIDPUserUpdate;
import com.saasstarterkit.user.dto.IAMUpdateUserDTO;
import com.saasstarterkit.user.dto.IAMUserDTO;
import com.saasstarterkit.user.dto.IAMUserExistDTO;
import com.saasstarterkit.user.dto.IAMUserInfoDTO;
import com.saasstarterkit.user.dto.UserModel;
import com.saasstarterkit.user.security.CurrentUser;
import com.saasstarterkit.user.config.IAMAdminClientConfig;
import com.saasstarterkit.user.config.IAMPropertyReader;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * Centralized IAM connector service interface
 * 
 * @author mohankumar.d
 *
 */
public interface IIAMConnector {
	public static enum RoleAction {
		Effective, 
		Available
	}
	public static enum AttributeAction {
		ADD, 
		Remove
	}
	public static enum UserState {
		Enable,
		Disable
	}
	public static enum MailAction {
		VERIFY_EMAIL,
		UPDATE_PASSWORD
	}
	
	public List<String> getCurrentUserRoles();
	public CurrentUser getCurrentUser();
	public IAMUserInfoDTO getIAMUser(String userid);
	public IAMUserDTO createIAMUser(IAMUserDTO iamUserDTO);
	public IAMUserExistDTO isUserExist(String username);
	public List<String> getIAMRoles();
	public List<String> getIAMUserRoles(String userId);
	public Boolean updateRoles(RoleAction roleAction, String userId, String... roles);
	public IAMUserDTO resetPassword(IAMUserDTO iamUserDTO, Boolean isCurrentUser);
	public IAMUpdateUserDTO updateIAMUser(IAMUpdateUserDTO iamUserDTO);
	public Boolean updateIAMUserState(UserState userState, String iamUserDTO);
	public Boolean addRole(String role);
	public Boolean removeRole(String role);
	public Boolean updateIDPUserName(List<IAMIDPUserUpdate> idpusers);
	public List<UserModel> getUsers(String filter, int firstindex, int maxresult);
	
	public Map<String, List<String>> getUserAttributes(String userId);
	public Boolean updateAttributes(String userId, Map<String, List<String>> userAttributes);
	public Boolean removeAttribute(String userId, String attributeKey);
	public Map<String, Object> getCurrentUserAttributes();
	public String triggerEmail(String userId, List<MailAction> emailActions);
	public int logoutAllUserSessions(String userId);
	public String createUserEndpoint(IAMPropertyReader keycloakPropertyReader);
	public RealmResource currentRealmResource(IAMPropertyReader keycloakPropertyReader);
	public CurrentUser getCurrentUserFromPrincipal();
}
