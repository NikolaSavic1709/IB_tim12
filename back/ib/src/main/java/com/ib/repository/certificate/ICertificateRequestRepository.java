package com.ib.repository.certificate;

import com.ib.model.certificate.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICertificateRequestRepository extends JpaRepository<CertificateRequest,Integer> {
}
