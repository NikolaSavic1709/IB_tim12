package com.ib.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ForgotPasswordDTO {
    @NotNull
    @NotBlank
    @NotEmpty
    private String activationType;
    @NotNull
    @NotBlank
    @NotEmpty
    private String activationResource;

    @Override
    public String toString() {
        return "ForgotPasswordDTO{" +
                "activationType='" + activationType + '\'' +
                ", activationResource='" + activationResource + '\'' +
                '}';
    }
}
