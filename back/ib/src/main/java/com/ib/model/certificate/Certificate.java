package com.ib.model.certificate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    StringBuilder sn = new StringBuilder();
//    return sn.append(UUID.randomUUID().toString())
//    .append(UUID.randomUUID().toString()).toString();

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "signature_algorithm")
    private String signatureAlgorithm;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "status", nullable = false)
    private CertificateStatus status;

    @Column(name = "type", nullable = false)
    private CertificateType type;

    @Column(name = "email")
    private String email;
}
