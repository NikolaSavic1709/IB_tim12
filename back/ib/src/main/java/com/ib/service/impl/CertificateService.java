package com.ib.service.impl;

import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateStatus;
import com.ib.repository.certificate.ICertificateRepository;
import com.ib.service.interfaces.ICertificateService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CertificateService extends JPAService<Certificate> implements ICertificateService {

    @Autowired
    private ICertificateRepository certificateRepository;
    @Override
    protected JpaRepository<Certificate, Integer> getEntityRepository() {
        return certificateRepository;
    }

    @Override
    public boolean isValid(String serialNumber) throws EntityNotFoundException{
        Optional<Certificate> certificateOptional= Optional.ofNullable(certificateRepository.findBySerialNumber(serialNumber));
        if(certificateOptional.isEmpty()) throw new EntityNotFoundException();
        Certificate certificate=certificateOptional.get();
        if(!isDigitalSignatureValid(certificate) || !isTrustedAuthority(certificate) || isCertificateRevoked(certificate) || isCertificateOutdated(certificate)) {
            certificate.setStatus(CertificateStatus.INVALID);
            return false;
        }
        certificate.setStatus(CertificateStatus.VALID);
        return true;
    }
    private boolean isDigitalSignatureValid(Certificate certificate){
        return true;
    }
    private boolean isTrustedAuthority(Certificate certificate){
        return true;
    }
    private boolean isCertificateRevoked(Certificate certificate){
        // ova metoda se ne mora koristiti ako ce svaki put kada se povuce ili ponovo objavi sertifikat promeni fleg u bazi
        return true;
    }
    private boolean isCertificateOutdated(Certificate certificate){
        return certificate.getStartDate().isAfter(LocalDateTime.now()) || certificate.getEndDate().isBefore(LocalDateTime.now());
    }
}
