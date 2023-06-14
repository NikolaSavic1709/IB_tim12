package com.ib.DTO;

import com.ib.model.certificate.CertificateType;
import jakarta.validation.constraints.Email;
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
public class RequestCreationDTO {
    @NotNull
    @NotEmpty
    @NotBlank
    private String signatureAlgorithm;
    
    private String issuer;

    @NotNull
    private CertificateType type;

    @NotNull
    @NotEmpty
    @NotBlank
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    private String email;

    @Override
    public String toString() {
        return "RequestCreationDTO{" +
                "signatureAlgorithm='" + signatureAlgorithm + '\'' +
                ", issuer='" + issuer + '\'' +
                ", type=" + type +
                ", email='" + email + '\'' +
                '}';
    }
}
