package com.ib.repository.certificate;

import com.ib.model.certificate.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ICertificateRequestRepository extends JpaRepository<CertificateRequest,Integer> {
    List<CertificateRequest> findAll();

    @Query("SELECT r FROM CertificateRequest r where r.certificate.email = :email")
    List<CertificateRequest> findAllByCertificateEmail(@Param("email") String email);
}
