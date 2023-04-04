package com.ib.DTO;

import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateStatus;
import com.ib.model.certificate.CertificateType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Date startDate;

    private Date endDate;

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
