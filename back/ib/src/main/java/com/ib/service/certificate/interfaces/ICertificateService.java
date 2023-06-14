package com.ib.service.certificate.interfaces;

import com.ib.DTO.RequestCreationDTO;
import com.ib.exception.ForbiddenException;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateRequest;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import com.ib.service.base.interfaces.IJPAService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

public interface ICertificateService extends IJPAService<Certificate> {
    Certificate getBySerialNumber(String serialNumber) throws EntityNotFoundException;

    X509Certificate generateCertificate(Certificate certificate) throws ForbiddenException;

    String getOwnerOfCertificate(String serialNumber);

    Certificate createCertificateMetadata(RequestCreationDTO requestCreation);

    byte[] getCertificatesInZip(String serialNumber, String authHeader);
}
