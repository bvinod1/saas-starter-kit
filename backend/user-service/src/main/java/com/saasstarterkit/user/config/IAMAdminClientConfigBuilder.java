package com.saasstarterkit.user.config;

public class IAMAdminClientConfigBuilder {
	
	private String serverUrl;
	private String realm;
	private String clientId;
	private String clientSecret;
	
	public IAMAdminClientConfigBuilder() {}

	public IAMAdminClientConfigBuilder serverUrl(String serverUrl) {
		this.serverUrl = serverUrl;
		return this;
	}
	
	public IAMAdminClientConfigBuilder realm(String realm) {
		this.realm = realm;
		return this;
	}
	
	public IAMAdminClientConfigBuilder clientId(String clientId) {
		this.clientId = clientId;
		return this;
	}
	
	public IAMAdminClientConfigBuilder clientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		return this;
	}
	
	public IAMAdminClientConfig build() {
		IAMAdminClientConfig adminClientConfig = new IAMAdminClientConfig();
		adminClientConfig.setServerUrl(serverUrl);
		adminClientConfig.setRealm(realm);
		adminClientConfig.setClientId(clientId);
		adminClientConfig.setClientSecret(clientSecret);
		return adminClientConfig;
	}
}
