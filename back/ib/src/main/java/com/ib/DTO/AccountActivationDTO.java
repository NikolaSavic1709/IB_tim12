package com.ib.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountActivationDTO {
    private String activationType;
    private String activationResource;

    @Min(value = 100000)
    @Max(value = 999999)
    private Integer activationCode;
}
