package com.serviceconnect.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyEmailRequest {
    private String email;

    @NotBlank(message = "Verification code or token is required")
    private String token;

    public VerifyEmailRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
