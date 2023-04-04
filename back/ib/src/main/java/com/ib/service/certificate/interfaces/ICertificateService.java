package com.ib.service.certificate.interfaces;

import com.ib.DTO.RequestCreationDTO;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateRequest;
import java.security.cert.X509Certificate;
import com.ib.service.base.interfaces.IJPAService;
import jakarta.persistence.EntityNotFoundException;

public interface ICertificateService extends IJPAService<Certificate> {
    Certificate getBySerialNumber(String serialNumber) throws EntityNotFoundException;

    boolean getAndCheck(String id);
    X509Certificate generateCertificate(Certificate certificate);

    String getOwnerOfCertificate(String serialNumber);

    Certificate createCertificateMetadata(RequestCreationDTO requestCreation);

    }
