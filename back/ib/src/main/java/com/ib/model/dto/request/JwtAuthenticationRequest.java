package com.ib.model.dto.request;

import jakarta.validation.constraints.*;

// DTO za login
public class JwtAuthenticationRequest {
    @NotNull
    @NotEmpty
    @NotBlank
    private String email;

    @NotNull
    @NotEmpty
    @NotBlank
    private String password;

    @NotNull
    @NotEmpty
    @NotBlank
    private String MFAType;

    public JwtAuthenticationRequest() {
        super();
    }

    public JwtAuthenticationRequest(String email, String password) {
        this.setEmail(email);
        this.setPassword(password);
    }

    public JwtAuthenticationRequest(String email, String password, String MFAType) {
        this.setEmail(email);
        this.setPassword(password);
        this.setMFAType(MFAType);
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMFAType() {
        return this.MFAType;
    }

    public void setMFAType(String MFAType) {
        this.MFAType = MFAType;
    }
}
