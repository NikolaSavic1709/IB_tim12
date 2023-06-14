package com.ib.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ib.model.certificate.CertificateRequest;
import com.ib.model.certificate.CertificateStatus;
import com.ib.model.certificate.CertificateType;
import com.ib.model.certificate.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateForRequestDTO {
    private RequestStatus requestStatus;
    private String rejectionReason;
    private String serialNumber;
    private String signatureAlgorithm;
    private String issuer;
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date startDate;
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date endDate;
    private CertificateStatus certificateStatus;
    private CertificateType type;
    private String email;

    public CertificateForRequestDTO(CertificateRequest request) {
        this.requestStatus = request.getStatus();
        this.rejectionReason = request.getRejectionReason();
        this.serialNumber = request.getCertificate().getSerialNumber();
        this.signatureAlgorithm = request.getCertificate().getSignatureAlgorithm();
        this.issuer = request.getCertificate().getIssuer();
        this.startDate = request.getCertificate().getStartDate();
        this.endDate = request.getCertificate().getEndDate();
        this.certificateStatus = request.getCertificate().getStatus();
        this.type = request.getCertificate().getType();
        this.email = request.getCertificate().getEmail();
    }

    @Override
    public String toString() {
        return "CertificateForRequestDTO{" +
                "requestStatus=" + requestStatus +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", signatureAlgorithm='" + signatureAlgorithm + '\'' +
                ", issuer='" + issuer + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", certificateStatus=" + certificateStatus +
                ", type=" + type +
                ", email='" + email + '\'' +
                '}';
    }
}
