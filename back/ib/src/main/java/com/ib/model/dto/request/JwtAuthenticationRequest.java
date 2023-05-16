package com.ib.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO za login
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JwtAuthenticationRequest {
    @NotNull
    @NotEmpty
    @NotBlank
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    private String email;

    @NotNull
    @NotEmpty
    @NotBlank
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=_!]).{8,}$")
    private String password;

    @NotNull
    @NotEmpty
    @NotBlank
    private String mfaType;

}
