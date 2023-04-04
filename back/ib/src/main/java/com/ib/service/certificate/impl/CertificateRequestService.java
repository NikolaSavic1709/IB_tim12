package com.ib.service.certificate.impl;

import com.ib.exception.InvalidUserException;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateRequest;
import com.ib.model.certificate.CertificateStatus;
import com.ib.model.certificate.RequestStatus;
import com.ib.model.users.User;
import com.ib.repository.certificate.ICertificateRequestRepository;
import com.ib.service.base.impl.JPAService;
import com.ib.service.certificate.interfaces.ICertificateRequestService;
import com.ib.service.certificate.interfaces.ICertificateService;
import com.ib.service.users.interfaces.IUserService;
import com.ib.utils.TokenUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.List;

@Service
public class CertificateRequestService implements ICertificateRequestService {

    private final ICertificateRequestRepository certificateRequestRepository;
    private final IUserService userService;
    private final TokenUtils tokenUtils;
    private final ICertificateService certificateService;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository, IUserService userService, TokenUtils tokenUtils, ICertificateService certificateService) {
        this.certificateRequestRepository = certificateRequestRepository;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
        this.certificateService = certificateService;
    }

    @Override
    public List<CertificateRequest> getRequests(Integer userId, String authHeader) throws EntityNotFoundException, InvalidUserException {
        User user= userService.get(userId);

        String token = authHeader.substring(7);
        if (tokenUtils.getIdFromToken(token)!=userId)
            throw new InvalidUserException("Invalid user");
        if (user == null){
            throw new EntityNotFoundException("User does not exists");
        }

        List<CertificateRequest> requests;
        if(user.getAuthority().getName().equals("ROLE_ADMIN"))
            requests=certificateRequestRepository.findAll();
        else
            requests = certificateRequestRepository.findAllByCertificateEmail(user.getEmail());
        return requests;
    }

    public Certificate acceptRequest(CertificateRequest certificateRequest, String token) {
        Certificate newCertificate = certificateRequest.getCertificate();
        X509Certificate certificate = certificateService.generateCertificate(newCertificate);
        if (certificate != null) {
            if (certificateService.getOwnerOfCertificate(newCertificate.getIssuer()).equals(tokenUtils.getEmailFromToken(token.substring(7)))) {
                newCertificate.setStatus(CertificateStatus.VALID);
            certificateService.save(newCertificate);
            certificateRequest.setStatus(RequestStatus.ACCEPTED);
            certificateRequestRepository.save(certificateRequest);
            return newCertificate;
        }
        }
        return null;
    }

    public void rejectRequest(Integer id, String rejectionReason, String token) {
        CertificateRequest rejectedRequest = certificateRequestRepository.findById(id).orElse(null);
        if (rejectedRequest != null) {
            if (certificateService.getOwnerOfCertificate(rejectedRequest.getCertificate().getIssuer()).equals(tokenUtils.getEmailFromToken(token.substring(7)))) {
                rejectedRequest.setRejectionReason(rejectionReason);
                rejectedRequest.setStatus(RequestStatus.REJECTED);
                certificateRequestRepository.save(rejectedRequest);
            }
        }
    }
}

