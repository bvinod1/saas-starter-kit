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
package com.saasstarterkit.user.config;

/**
 * 
 * @author Mohankumar.D
 *
 */
public class IAMConstants {
    
    private IAMConstants() {
        
    }

    public static final String ROLE_API_ACCESS = "apiAccess";
    public static final String ROLE_GUI_ACCESS = "guiAccess";
    public static final String ROLE_ADMINISTRATOR = "administrator";
    public static final String ROLE_USER_MANAGEMENT = "userManagement";
    //public static final ImmutableList<String> ROLE_KEYCLOAK_DEFAULT_EXCLUDED = ImmutableList.of("uma_authorization", "offline_access");
    
    public static final String GRANT_TYPE = "grant_type";
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String USERNAME = "username";
	public static final String KEYCLOAK_PD = "password";
	public static final String SCOPE = "scope";
	
	public static final String REFRESH_TOKEN = "refresh_token";
	
	public static final String USER_NOT_FOUND = "User not found";
	public static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
	
	//NOTIFICATION
	public static final String NOTIFICATION_CLIENTID = "iam.notification.clientId";
	public static final String NOTIFICATION_REDIRECT_URI = "iam.notification.redirect-uri";
	public static final String KNOTIFICATION_LIFESPAN = "iam.notification.lifespan";
}
