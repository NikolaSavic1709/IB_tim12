package com.ib.service.certificate.impl;

import com.ib.exception.CertificateAlreadyRevokedException;
import com.ib.exception.ForbiddenException;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateType;
import com.ib.repository.certificate.ICertificateRepository;
import com.ib.service.CertificateFileStorage;
import com.ib.service.certificate.interfaces.ICertificateService;
import com.ib.service.certificate.interfaces.ICertificateValidationService;
import com.ib.utils.TokenUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CertificateValidationService implements ICertificateValidationService {

    private final ICertificateRepository certificateRepository;
    private final ICertificateService certificateService;
    private final CertificateFileStorage certificateFileStorage;
    private final TokenUtils tokenUtils;

    @Autowired
    public CertificateValidationService(ICertificateRepository certificateRepository, ICertificateService certificateService, CertificateFileStorage certificateFileStorage, TokenUtils tokenUtils) {
        this.certificateRepository = certificateRepository;
        this.certificateService = certificateService;
        this.certificateFileStorage = certificateFileStorage;
        this.tokenUtils = tokenUtils;
    }

    @Override
    public void revokeCertificate(String serialNumber, String revocationReason, String authHeader) throws EntityNotFoundException, ForbiddenException, CertificateAlreadyRevokedException {
        Certificate certificate=certificateService.getBySerialNumber(serialNumber);
        String token = authHeader.substring(7);

        if (!Objects.equals(tokenUtils.getEmailFromToken(token), certificate.getEmail()) && !tokenUtils.getRoleFromToken(token).equals("ADMIN"))
            throw new ForbiddenException("Permission denied");

        if(certificate.isRevoked())
            throw new CertificateAlreadyRevokedException("Certificate is already revoked");

        certificate.setRevoked(true);
        certificate.setRevocationReason(revocationReason);
        certificateRepository.save(certificate);
        revokeAllSubordinates(certificate.getSerialNumber(),revocationReason);

    }
    @Override
    public boolean getAndCheck(String serialNumber) throws EntityNotFoundException {
        Optional<Certificate> certificate= Optional.ofNullable(certificateRepository.findBySerialNumber(serialNumber));
        if(certificate.isEmpty()) throw new EntityNotFoundException();
        return isValid(certificate.get());
    }
    @Override
    public boolean checkByCopy(MultipartFile file) throws IOException, CertificateException, EntityNotFoundException {
        byte[] bytes = file.getBytes();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(bytes));
        String serialNumber= String.valueOf(cert.getSerialNumber());
        return getAndCheck(serialNumber);

    }
    private boolean isValid(Certificate certificate){
        return !certificate.isRevoked() && isDigitalSignatureValid(certificate) && !isCertificateOutdated(certificate);
    }
    private boolean isDigitalSignatureValid(Certificate certificate){
        try {
            if (certificate.getIssuer()==null) return true;
            X509Certificate cert=certificateFileStorage.getCertificateFromStorage(certificate.getSerialNumber());
            X509Certificate issuerCert=certificateFileStorage.getCertificateFromStorage(certificate.getIssuer());

            cert.verify(issuerCert.getPublicKey());
            return true;
        } catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | SignatureException |
                 NoSuchProviderException e) {
            return false;
        }
    }
    private boolean isTrustedAuthority(Certificate certificate) {
        if (certificate.getType().equals(CertificateType.ROOT) || isValid(certificateRepository.findBySerialNumber(certificate.getIssuer())))
            return true;
        return false;
    }
    private boolean isCertificateOutdated(Certificate certificate){
        LocalDateTime now = LocalDateTime.now();
        return certificate.getStartDate().after(Date.from(now.atZone(ZoneId.systemDefault()).toInstant())) || certificate.getEndDate().before(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
    }
    private void revokeAllSubordinates(String issuerSerialNumber, String revocationReason){
        List<Certificate> validSubordinates=certificateService.getAll().parallelStream().filter(certificate -> isAncestor(certificate,issuerSerialNumber) && !certificate.isRevoked()).toList();
        for(Certificate certificate: validSubordinates){
            certificate.setRevocationReason(revocationReason);
            certificate.setRevoked(true);
            certificateRepository.save(certificate);
        }
    }
    private boolean isAncestor(Certificate certificate, String issuerSerialNumber)
    {
        if(certificate.getIssuer() == null)
            return false;
        if(certificate.getIssuer().equals(issuerSerialNumber))
            return true;
        return isAncestor(certificateService.getBySerialNumber(certificate.getIssuer()),issuerSerialNumber);
    }
}
