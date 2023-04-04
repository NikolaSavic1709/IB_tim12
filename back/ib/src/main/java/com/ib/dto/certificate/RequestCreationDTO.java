package com.ib.dto.certificate;

import com.ib.model.certificate.CertificateStatus;
import com.ib.model.certificate.CertificateType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
}
