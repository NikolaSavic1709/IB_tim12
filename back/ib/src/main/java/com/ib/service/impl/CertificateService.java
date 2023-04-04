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
    public boolean getAndCheck(String serialNumber) throws EntityNotFoundException{
        Optional<Certificate> certificate= Optional.ofNullable(certificateRepository.findBySerialNumber(serialNumber));
        if(certificate.isEmpty()) throw new EntityNotFoundException();
        return isValid(certificate.get());
    }
    private boolean isValid(Certificate certificate){
        if(!isDigitalSignatureValid(certificate) || !isTrustedAuthority(certificate) || isCertificateRevoked(certificate) || isCertificateOutdated(certificate)) {
            certificate.setStatus(CertificateStatus.INVALID);
            return false;
        }
        certificate.setStatus(CertificateStatus.VALID);
        return true;
    }
    private boolean isDigitalSignatureValid(Certificate certificate){
        //konverzija u java certificate
        //citanje iz fajla
        //cert.verify(keyPairIssuer.getPublic());
        return true;
    }
    private boolean isTrustedAuthority(Certificate certificate){
        //konverzija u java certificate
        // uzima se roditelj sertifikata
        // cert.getIssuer.getID po njemu se uzima sertifikat iz keystore-a
        // zove se isValid za njega
        // ako je roditelj null to je to
        //if(roditelj==null || isValid(roditelj)) return true;
        // return false;
        return true;
    }
    private boolean isCertificateRevoked(Certificate certificate){
        // ova metoda se ne mora koristiti ako ce svaki put kada se povuce ili ponovo objavi sertifikat promeni fleg u bazi
        return certificate.getStatus()==CertificateStatus.INVALID;
    }
    private boolean isCertificateOutdated(Certificate certificate){
        return certificate.getStartDate().isAfter(LocalDateTime.now()) || certificate.getEndDate().isBefore(LocalDateTime.now());
    }
}
