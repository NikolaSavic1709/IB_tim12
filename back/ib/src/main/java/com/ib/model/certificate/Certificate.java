package com.ib.model.certificate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
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
@Entity
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "signature_algorithm")
    private String signatureAlgorithm;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "valid_from")
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date startDate;

    @Column(name = "valid_to")
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date endDate;

    @Column(name = "status", nullable = false)
    private CertificateStatus status;

    @Column(name = "type", nullable = false)
    private CertificateType type;

    @Column(name = "email")
    private String email;

    @Column(name="is_revoked")
    private boolean isRevoked;
    @Column(name="revocation_reason")
    private String revocationReason;

    public Certificate(String serialNumber, String signatureAlgorithm, String issuer, CertificateStatus status, CertificateType type, String email, String revocationReason, boolean isRevoked) {
        this.serialNumber = serialNumber;
        this.signatureAlgorithm = signatureAlgorithm;
        this.issuer = issuer;
        this.status = status;
        this.type = type;
        this.email = email;
        this.isRevoked=isRevoked;
        this.revocationReason=revocationReason;
    }
}
