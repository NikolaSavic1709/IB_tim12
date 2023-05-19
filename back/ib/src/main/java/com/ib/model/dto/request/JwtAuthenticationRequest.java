package com.ib.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// DTO za login
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
    private String mfaType;

    @Override
    public String toString() {
        return "JwtAuthenticationRequest{" +
                "email='" + email + '\'' +
                ", password='" + new BCryptPasswordEncoder().encode(password) + '\'' +
                ", mfaType='" + mfaType + '\'' +
                '}';
    }
}
