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

import com.saasstarterkit.user.config.IAMConstants;
import com.saasstarterkit.user.config.IAMPropertyReader;
import com.saasstarterkit.user.dto.UserCredentials;
import com.saasstarterkit.user.exception.IAMAuthenticationException;
import com.saasstarterkit.user.security.CurrentUserProvider;
import com.saasstarterkit.user.service.interfaces.IIAMAuthentication;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This user authentication service gives implementation of IIAMAuthentication
 * interface
 * 
 * @author mohankumar.d
 *
 */
@Service
public class IAMAuthentication implements IIAMAuthentication {
	
	private final CurrentUserProvider currentUserProvider;
	
	private final IAMPropertyReader keycloakPropertyReader;

	public IAMAuthentication(CurrentUserProvider currentUserProvider, IAMPropertyReader keycloakPropertyReader) {
		this.currentUserProvider = currentUserProvider;
		this.keycloakPropertyReader = keycloakPropertyReader;
	}

	/**
	 * This method is used to create a user's authentication details
	 * 
	 * @param userCredentials -[Mindsprint IAM user credentials object
	 * @param _clientApp      properties prefix name
	 * @param grandType       based on the user request
	 * @return -[return user's access token
	 * @exception IAMAuthenticationException on error
	 */
	@Override
	public String getUserAuthToken(UserCredentials userCredentials, String _clientApp, GrantType grandType,
			Scope scope) {
		String responseToken;
		try {
			_clientApp = _clientApp.isEmpty() || _clientApp.isBlank() ? "" : _clientApp + ".";
			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair(IAMConstants.GRANT_TYPE, grandType.toString()));
			urlParameters.add(new BasicNameValuePair(IAMConstants.CLIENT_ID,
					keycloakPropertyReader.getProperty(_clientApp + "keycloak.resource")));
			urlParameters.add(new BasicNameValuePair(IAMConstants.USERNAME, userCredentials.getUsername()));
			urlParameters.add(new BasicNameValuePair(IAMConstants.KEYCLOAK_PD, userCredentials.getPassword()));
			urlParameters.add(new BasicNameValuePair(IAMConstants.CLIENT_SECRET,
					keycloakPropertyReader.getProperty(_clientApp + "keycloak.credentials.secret")));
			if (!"none".equalsIgnoreCase(scope.toString()))
				urlParameters.add(new BasicNameValuePair(IAMConstants.SCOPE, scope.toString()));

			responseToken = (String)sendPost(urlParameters, _clientApp, grandType);

		} catch (Exception e) {
			throw new IAMAuthenticationException(e.getMessage());
		}

		return responseToken;
	}

	/**
	 * This method is used to create a user's refresh token
	 * 
	 * @param _refreshToken current refresh token
	 * @param _clientApp    properties prefix name
	 * @param grandType     based on the user request
	 * @return -[return user's refresh token
	 * @exception IAMAuthenticationException on error
	 */
	@Override
	public String getUserRefreshToken(String _refreshToken, String _clientApp, GrantType grandType) {
		String responseToken;
		try {
			responseToken = (String)generatePostRequest(_refreshToken, _clientApp, grandType);
		} catch (Exception e) {
			throw new IAMAuthenticationException(e.getMessage());
		}
		return responseToken;
	}

	/**
	 * This method is helps to remove current user session
	 * 
	 * @return return true on success
	 * @exception IAMAuthenticationException on error
	 */
	public Boolean logout() {
		try {
			currentUserProvider.logout();
			return true;
		} catch (Exception e) {
			throw new IAMAuthenticationException(e.getMessage());
		}
	}
	
	/***
	 * This method is used to remove a user session
	 * 
	 * @param _refreshToken current refresh token
	 * @param _clientApp    properties prefix name
	 * @param grandType     based on the user request
	 * @return -[return user remove response
	 * @exception IAMAuthenticationException on error
	 */
	@Override
	public String logoutUserSession(String _refreshToken, String _clientApp, GrantType grandType) {
		String response;
		try {
			HttpResponse httpResponse = (HttpResponse)generatePostRequest(_refreshToken, _clientApp, grandType);
			response = httpResponse.getStatusLine().getStatusCode() == 204 ? "User Session Logged out" : "Error in User Session Logout";
			
		} catch (Exception e) {
			throw new IAMAuthenticationException(e.getMessage());
		}
		return response;
	}

	private Object sendPost(List<NameValuePair> urlParameters, String _clientApp, GrantType grandType)
			throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = getHttpPost(_clientApp, grandType);

		httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
		HttpResponse response = client.execute(httpPost);
		StringBuilder result;
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			result = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (Exception e) {
			return response;
		}

		return result.toString();
	}

	private HttpPost getHttpPost(String _clientApp, GrantType grandType) {
		String AUTH_URL = keycloakPropertyReader.getProperty(_clientApp + "keycloak.auth-server-url");
		String REALM = keycloakPropertyReader.getProperty(_clientApp + "keycloak.realm");

		HttpPost httpPost = null;
		if (grandType.equals(GrantType.password) || grandType.equals(GrantType.refresh_token))
			httpPost = new HttpPost(AUTH_URL + "/realms/" + REALM + "/protocol/openid-connect/token");
		else if(grandType.equals(GrantType.logout))
			httpPost = new HttpPost(AUTH_URL + "/realms/" + REALM + "/protocol/openid-connect/logout");

		return httpPost;
	}

	private Object generatePostRequest(String _refreshToken, String _clientApp, GrantType grandType) throws Exception {
		_clientApp = _clientApp.isEmpty() || _clientApp.isBlank() ? "" : _clientApp + ".";
		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair(IAMConstants.GRANT_TYPE, grandType.toString()));
		urlParameters.add(new BasicNameValuePair(IAMConstants.REFRESH_TOKEN, _refreshToken));

		urlParameters.add(new BasicNameValuePair(IAMConstants.CLIENT_ID,
				keycloakPropertyReader.getProperty(_clientApp + "keycloak.resource")));
		urlParameters.add(new BasicNameValuePair(IAMConstants.CLIENT_SECRET,
				keycloakPropertyReader.getProperty(_clientApp + "keycloak.credentials.secret")));
		return sendPost(urlParameters, _clientApp, grandType);
	}
}
