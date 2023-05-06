package com.ib.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResetPasswordDTO {
    @NotNull
    @NotBlank
    @NotEmpty
    private String activationType;
    @NotNull
    @NotBlank
    @NotEmpty
    private String activationResource;

    @Min(value = 100000)
    @Max(value = 999999)
    private Integer code;

    @NotNull
    @NotEmpty
    @NotBlank
   // @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z])(?!.*[^a-zA-Z0-9@#$^+=])(.{8,15})$")
    private String newPassword;
}
