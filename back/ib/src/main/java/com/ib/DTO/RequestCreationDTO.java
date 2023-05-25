package com.ib.DTO;

import com.ib.model.certificate.CertificateType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequestCreationDTO {

    private String signatureAlgorithm;

    private String issuer;

    private CertificateType type;

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
