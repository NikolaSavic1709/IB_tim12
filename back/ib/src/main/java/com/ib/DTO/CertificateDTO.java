package com.ib.DTO;

import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateStatus;
import com.ib.model.certificate.CertificateType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private CertificateType type;

    private String email;

    public CertificateDTO(Certificate certificate)
    {
        this.email=certificate.getEmail();
        this.startDate=certificate.getStartDate();
        this.endDate=certificate.getEndDate();
        this.type=certificate.getType();
    }
}
