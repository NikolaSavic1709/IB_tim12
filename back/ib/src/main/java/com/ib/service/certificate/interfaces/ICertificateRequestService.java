package com.ib.service.certificate.interfaces;

import com.ib.model.certificate.CertificateRequest;

import java.util.List;

public interface ICertificateRequestService {
    List<CertificateRequest> getRequests(Integer userId);
}
