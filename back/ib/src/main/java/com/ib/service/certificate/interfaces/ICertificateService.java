package com.ib.service.certificate.interfaces;

import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateRequest;
import java.security.cert.X509Certificate;
import com.ib.service.base.interfaces.IJPAService;

public interface ICertificateService extends IJPAService<Certificate> {
    boolean getAndCheck(String id);
    X509Certificate generateCertificate(Certificate certificate);

    Certificate acceptRequest(CertificateRequest certificateRequest);
    void rejectRequest(String serialNumber, String rejectionRequest);
}
