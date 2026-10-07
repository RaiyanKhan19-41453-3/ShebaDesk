package com.shebadesk.dto;

public class LoginResponseDTO {
    private String tokenType = "Bearer";
    private String accessToken;
    private long expiresInSeconds;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String accessToken, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
