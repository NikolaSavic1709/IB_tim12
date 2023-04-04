package com.ib.service.certificate.impl;

import com.ib.DTO.RequestCreationDTO;
import com.ib.exception.CertificateCreationException;
import com.ib.exception.ForbiddenException;
import com.ib.exception.InvalidUserException;
import com.ib.model.certificate.*;
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
public class CertificateRequestService extends JPAService<CertificateRequest> implements ICertificateRequestService {

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
    protected JpaRepository<CertificateRequest, Integer> getEntityRepository() {
        return certificateRequestRepository;
    }

    @Override
    public List<CertificateRequest> getRequests(Integer userId, String authHeader) throws EntityNotFoundException, InvalidUserException {
        User user= userService.get(userId);

        String token = authHeader.substring(7);
        if (tokenUtils.getIdFromToken(token)!=userId)
            throw new InvalidUserException("Permission denied");
        if (user == null){
            throw new EntityNotFoundException("User does not exists");
        }

        List<CertificateRequest> requests;
        if(user.getAuthority().getName().equals("ROLE_ADMIN"))
            requests = certificateRequestRepository.findAll();
        else
            requests = certificateRequestRepository.findAllByCertificateEmail(user.getEmail());
        return requests;
    }

    @Override
    public CertificateRequest createRequest(RequestCreationDTO requestCreation, String authHeader) throws EntityNotFoundException,
                                                                                                          ForbiddenException,
                                                                                                          CertificateCreationException{

        String token = authHeader.substring(7);
        String role = tokenUtils.getRoleFromToken(token);
        if (!role.equals("ROLE_ADMIN") && requestCreation.getType().equals(CertificateType.ROOT))
            throw new ForbiddenException("Permission denied");

        String userEmail = tokenUtils.getEmailFromToken(token);
        if (!requestCreation.getEmail().equals(userEmail))
            throw new ForbiddenException("You can request a certificate only for your own");

        Certificate issuerCert = certificateService.getBySerialNumber(requestCreation.getIssuer());
        Certificate certificateMetadata = certificateService.createCertificateMetadata(requestCreation);

        CertificateRequest request = new CertificateRequest(certificateMetadata);

        if (role.equals("ROLE_ADMIN") || issuerCert.getEmail().equals(userEmail))
            instantApproval(request);

        return save(request);
    }

    private void instantApproval(CertificateRequest certificateRequest) throws CertificateCreationException {
        Certificate certificateMetadata = certificateRequest.getCertificate();
        X509Certificate certificate = certificateService.generateCertificate(certificateMetadata);
        if (certificate != null) {
            certificateMetadata.setStatus(CertificateStatus.VALID);
            certificateRequest.setCertificate(certificateService.save(certificateMetadata));
            certificateRequest.setStatus(RequestStatus.ACCEPTED);
        }
        throw new CertificateCreationException("Certificate creation failed");
    }

    @Override
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

    @Override
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

