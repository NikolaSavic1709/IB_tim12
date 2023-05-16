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
import com.ib.service.certificate.interfaces.ICertificateValidationService;
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
    private final ICertificateValidationService certificateValidationService;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository, IUserService userService, TokenUtils tokenUtils, ICertificateService certificateService, ICertificateValidationService certificateValidationService) {
        this.certificateRequestRepository = certificateRequestRepository;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
        this.certificateService = certificateService;
        this.certificateValidationService = certificateValidationService;
    }

    @Override
    protected JpaRepository<CertificateRequest, Integer> getEntityRepository() {
        return certificateRequestRepository;
    }

    @Override
    public List<CertificateRequest> getRequests(Integer userId, String authHeader) throws EntityNotFoundException, InvalidUserException {
        User user= userService.get(userId);

        String token = authHeader.substring(7);
        if (user == null){
            throw new EntityNotFoundException("User does not exists");
        }
        if (tokenUtils.getIdFromToken(token)!=userId && !tokenUtils.getRoleFromToken(token).equals("ADMIN"))
            throw new InvalidUserException("Permission denied");
        List<CertificateRequest> requests;
        if(tokenUtils.getRoleFromToken(token).equals("ADMIN") && tokenUtils.getIdFromToken(token)==userId)
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
        if (!role.equals("ADMIN") && requestCreation.getType().equals(CertificateType.ROOT))
            throw new ForbiddenException("Permission denied");

        if (requestCreation.getIssuer()==null && !requestCreation.getType().equals(CertificateType.ROOT))
            throw new ForbiddenException("Couldn't create non-root certificate without issuer");

        String userEmail = tokenUtils.getEmailFromToken(token);
        if (!requestCreation.getEmail().equals(userEmail))
            throw new ForbiddenException("You can request a certificate only for your own");

        if(requestCreation.getIssuer()!=null && requestCreation.getType().equals(CertificateType.ROOT))
            throw new ForbiddenException("Root certificates mustn't have issuer");


        Certificate issuerCert=new Certificate();
        if(requestCreation.getIssuer()!=null) {
            issuerCert = certificateService.getBySerialNumber(requestCreation.getIssuer());
            // ili nije aktivan ili nije validan (povucen, istekao...)
            if(issuerCert.getStatus().equals(CertificateStatus.INVALID) || !certificateValidationService.getAndCheck(requestCreation.getIssuer()))
                throw new EntityNotFoundException("Invalid issuer");
        }
        Certificate certificateMetadata = certificateService.createCertificateMetadata(requestCreation);

        CertificateRequest request = new CertificateRequest(certificateMetadata);

        if (role.equals("ADMIN") || issuerCert.getEmail().equals(userEmail))
            instantApproval(request);

        return save(request);
    }

    private void instantApproval(CertificateRequest certificateRequest) throws CertificateCreationException, ForbiddenException {
        Certificate certificateMetadata = certificateRequest.getCertificate();
        X509Certificate certificate = certificateService.generateCertificate(certificateMetadata);
        if (certificate != null) {
            certificateMetadata.setStatus(CertificateStatus.VALID);
            certificateRequest.setCertificate(certificateService.save(certificateMetadata));
            certificateRequest.setStatus(RequestStatus.ACCEPTED);
            return;
        }
        throw new CertificateCreationException("Certificate creation failed");
    }

    @Override
    public Certificate acceptRequest(String serialNumber, String token) throws ForbiddenException {
        Certificate newCertificate = certificateService.getBySerialNumber(serialNumber);
        CertificateRequest certificateRequest = certificateRequestRepository.findByCertificate(newCertificate).orElse(null);
        X509Certificate certificate = certificateService.generateCertificate(newCertificate);
        if (certificate != null && certificateRequest.getStatus().equals(RequestStatus.PENDING) ) {
            if ( certificateService.getOwnerOfCertificate(newCertificate.getIssuer()).equals(tokenUtils.getEmailFromToken(token.substring(7)))) {
                newCertificate.setStatus(CertificateStatus.VALID);
                certificateService.save(newCertificate);
                certificateRequest.setStatus(RequestStatus.ACCEPTED);
                certificateRequestRepository.save(certificateRequest);
                return newCertificate;
            }
        } else throw new ForbiddenException("Status is not PENDING!");

        return null;
    }

    @Override
    public void rejectRequest(String serialNumber, String rejectionReason, String token) throws ForbiddenException {
        Certificate newCertificate = certificateService.getBySerialNumber(serialNumber);
        CertificateRequest rejectedRequest = certificateRequestRepository.findByCertificate(newCertificate).orElse(null);
        if (rejectedRequest != null && rejectedRequest.getStatus().equals(RequestStatus.PENDING)) {
            if (certificateService.getOwnerOfCertificate(rejectedRequest.getCertificate().getIssuer()).equals(tokenUtils.getEmailFromToken(token.substring(7)))) {
                rejectedRequest.setRejectionReason(rejectionReason);
                rejectedRequest.setStatus(RequestStatus.REJECTED);
                certificateRequestRepository.save(rejectedRequest);
            } else throw new ForbiddenException("Status is not PENDING!");
        }else throw new ForbiddenException("Status is not PENDING!");
    }
}

