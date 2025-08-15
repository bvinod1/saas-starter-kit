package com.saasstarterkit.user.config;

/**
 * Bean created for cross realm configuration
 * Used internally. Not exposed to client as any response object
 * 
 */
public class IAMCredentials {

	private String secret;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
