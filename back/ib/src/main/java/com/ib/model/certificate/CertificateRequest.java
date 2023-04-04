package com.ib.model.certificate;

import com.ib.model.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class CertificateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "certificate")
    private Certificate certificate;

    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @Column(name = "reject_reason")
    private String rejectionReason;

    public CertificateRequest(Certificate certificate) {
        this.certificate = certificate;
        this.status = RequestStatus.PENDING;
        this.rejectionReason = null;
    }
}
