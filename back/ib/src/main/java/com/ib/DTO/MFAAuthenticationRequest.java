package com.ib.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MFAAuthenticationRequest {

    @NotNull
    @NotEmpty
    @NotBlank
    private String email;

    @NotNull
    @NotEmpty
    @NotBlank
    private String password;

    @Min(value = 100000)
    @Max(value = 999999)
    private Integer token;


    @Override
    public String toString() {
        return "MFAAuthenticationRequest{" +
                "email='" + email + '\'' +
                ", password='" + new BCryptPasswordEncoder().encode(password) + '\'' +
                ", token=" + new BCryptPasswordEncoder().encode(token.toString()) +
                '}';
    }
}
