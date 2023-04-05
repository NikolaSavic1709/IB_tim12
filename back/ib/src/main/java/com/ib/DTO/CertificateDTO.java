package com.ib.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateStatus;
import com.ib.model.certificate.CertificateType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateDTO {
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date startDate;

    @JsonFormat(pattern= "dd-MM-yyyy")
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
