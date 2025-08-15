package com.saasstarterkit.auth.dto;

public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private String tokenType;

    // Default constructor
    public TokenResponse() {}

    // Constructor with all fields
    public TokenResponse(String accessToken, String refreshToken, Long expiresIn, Long refreshExpiresIn, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.tokenType = tokenType;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Long getExpiresIn() { return expiresIn; }
    public Long getRefreshExpiresIn() { return refreshExpiresIn; }
    public String getTokenType() { return tokenType; }

    // Setters
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public void setRefreshExpiresIn(Long refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}
