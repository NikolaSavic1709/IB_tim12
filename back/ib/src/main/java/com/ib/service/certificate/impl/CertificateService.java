package com.ib.service.certificate.impl;

import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateStatus;
import com.ib.model.users.User;
import com.ib.repository.certificate.ICertificateRepository;
import com.ib.service.CertificateFileStorage;
import com.ib.service.UserService;
import com.ib.service.base.impl.JPAService;
import com.ib.service.certificate.interfaces.ICertificateService;
import jakarta.persistence.EntityNotFoundException;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

@Service
public class CertificateService extends JPAService<Certificate> implements ICertificateService {

    @Autowired
    private ICertificateRepository certificateRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CertificateFileStorage certificateFileStorage;

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
        return true;
    }
    private boolean isCertificateOutdated(Certificate certificate){
        LocalDateTime now = LocalDateTime.now();
        return certificate.getStartDate().after(Date.from(now.atZone(ZoneId.systemDefault()).toInstant())) || certificate.getEndDate().before(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
    }

    private X509Certificate generateCertificate(Certificate certificateRequest) {
        try {
            JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");

            signerBuilder = signerBuilder.setProvider("BC");
            KeyPair keyPairSubject = generateKeyPair();
            //TODO: get issuer private key from key folders
            ContentSigner contentSigner = signerBuilder.build(keyPairSubject.getPrivate());

            X500Name issuerX500Name = null;
            if (certificateRequest.getIssuer()!=null){
                Certificate issuerCert =  certificateRepository.findBySerialNumber(certificateRequest.getIssuer());

                User issuer = userService.findByEmail(issuerCert.getEmail());
                X500NameBuilder builderIssuer = generateX500Name(issuer);
                builderIssuer.addRDN(BCStyle.UID, issuerCert.getSerialNumber());

                issuerX500Name = builderIssuer.build();
            }

            User subject = userService.findByEmail(certificateRequest.getEmail());
            X500NameBuilder builderSubject = generateX500Name(subject);
            String sn = generateSerialNumber();
            builderSubject.addRDN(BCStyle.UID, sn);

            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                    issuerX500Name,
                    new BigInteger(sn),
                    certificateRequest.getStartDate(),
                    certificateRequest.getEndDate(),
                    builderSubject.build(),
                    keyPairSubject.getPublic());

            X509CertificateHolder certHolder = certGen.build(contentSigner);

            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            certConverter = certConverter.setProvider("BC");
            X509Certificate cert = certConverter.getCertificate(certHolder);

            certificateFileStorage.exportCertificate(cert);
            certificateFileStorage.exportPrivateKey(keyPairSubject.getPrivate(), sn);

            return cert;
        } catch (IllegalArgumentException | IllegalStateException | OperatorCreationException | CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    private X500NameBuilder generateX500Name (User user) {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, user.getName() + " " + user.getSurname());
        builder.addRDN(BCStyle.SURNAME, user.getSurname());
        builder.addRDN(BCStyle.GIVENNAME, user.getName());
        builder.addRDN(BCStyle.O, "Tim12.org");
        builder.addRDN(BCStyle.OU, "Informaciona bezbednost");
        builder.addRDN(BCStyle.C, "RS");
        builder.addRDN(BCStyle.E, user.getEmail());

        return builder;
    }

    private static String generateSerialNumber() {
        StringBuilder sn = new StringBuilder();
        return sn.append(UUID.randomUUID().toString()).append(UUID.randomUUID().toString()).toString();
    }

}
