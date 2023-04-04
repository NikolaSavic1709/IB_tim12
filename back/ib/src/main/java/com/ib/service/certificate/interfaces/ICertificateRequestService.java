package com.ib.service.certificate.interfaces;

import com.ib.exception.InvalidUserException;
import com.ib.model.certificate.CertificateRequest;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public interface ICertificateRequestService {
    List<CertificateRequest> getRequests(Integer userId, String authHeader) throws EntityNotFoundException, InvalidUserException;
}
