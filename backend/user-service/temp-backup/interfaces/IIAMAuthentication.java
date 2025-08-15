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

import com.saasstarterkit.user.dto.UserCredentials;

/**
 * Centralized IAM User Authentication service interface
 * 
 * @author mohankumar.d
 *
 */
public interface IIAMAuthentication {
	public static enum GrantType {
		password, 
		refresh_token,
		logout
	}
	
	public static enum Scope {
		offline_access,
		none
	}
	
	public String getUserAuthToken(UserCredentials userCredentials, String _clientApp, GrantType grandType, Scope scope);
	public String getUserRefreshToken(String _refreshToken, String _clientApp, GrantType grandType);
	public Boolean logout();
	public String logoutUserSession(String _refreshToken, String _clientApp, GrantType grandType);
}
