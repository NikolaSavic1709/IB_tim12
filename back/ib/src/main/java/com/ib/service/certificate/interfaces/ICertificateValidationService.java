package com.ib.service.certificate.interfaces;

import com.ib.exception.CertificateAlreadyRevokedException;
import com.ib.exception.ForbiddenException;
import com.ib.exception.InvalidUserException;
import com.ib.model.certificate.Certificate;
import com.ib.service.base.interfaces.IJPAService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.cert.CertificateException;

public interface ICertificateValidationService {
    void revokeCertificate(String serialNumber, String revocationReason, String authHeader) throws EntityNotFoundException, ForbiddenException, CertificateAlreadyRevokedException;

    boolean getAndCheck(String id);

    boolean checkByCopy(MultipartFile file) throws IOException, CertificateException;
}
