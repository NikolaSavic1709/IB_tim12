package com.ib.service.certificate.interfaces;

import com.ib.DTO.RequestCreationDTO;
import com.ib.exception.CertificateCreationException;
import com.ib.exception.ForbiddenException;
import com.ib.exception.InvalidUserException;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateRequest;
import com.ib.service.base.interfaces.IJPAService;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public interface ICertificateRequestService {
    List<CertificateRequest> getRequests(Integer userId, String authHeader) throws EntityNotFoundException, InvalidUserException;

    CertificateRequest createRequest(RequestCreationDTO requestCreation, String authHeader) throws EntityNotFoundException, ForbiddenException, CertificateCreationException;
    Certificate acceptRequest(CertificateRequest certificateRequest, String token) throws ForbiddenException;
    void rejectRequest(Integer id, String rejectionRequest, String token);
}
