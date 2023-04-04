package com.ib.service.certificate.impl;

import com.ib.model.certificate.CertificateRequest;
import com.ib.model.users.User;
import com.ib.repository.certificate.ICertificateRequestRepository;
import com.ib.service.certificate.interfaces.ICertificateRequestService;
import com.ib.service.users.interfaces.IUserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateRequestService implements ICertificateRequestService {

    private final ICertificateRequestRepository certificateRequestRepository;
    private final IUserService userService;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository, IUserService userService) {
        this.certificateRequestRepository = certificateRequestRepository;
        this.userService = userService;
    }

    @Override
    public List<CertificateRequest> getRequests(Integer userId) throws EntityNotFoundException{
        User user= userService.get(userId);
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
}
