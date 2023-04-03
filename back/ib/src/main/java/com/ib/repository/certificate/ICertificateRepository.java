package com.ib.repository.certificate;

import com.ib.model.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICertificateRepository extends JpaRepository<Certificate,Integer> {

    Certificate findBySerialNumber(String serialNumber);
}
