package com.ib.service.certificate.impl;

import com.ib.DTO.RequestCreationDTO;
import com.ib.exception.ForbiddenException;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateStatus;
import com.ib.model.certificate.CertificateType;
import com.ib.model.users.User;
import com.ib.repository.certificate.ICertificateRepository;
import com.ib.service.CertificateFileStorage;
import com.ib.service.base.impl.JPAService;
import com.ib.service.certificate.interfaces.ICertificateService;
import com.ib.service.users.impl.UserService;
import com.ib.utils.TokenUtils;
import jakarta.persistence.EntityNotFoundException;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

@Service
public class CertificateService extends JPAService<Certificate> implements ICertificateService {

    private final ICertificateRepository certificateRepository;

    private final UserService userService;

    private final CertificateFileStorage certificateFileStorage;
    private final TokenUtils tokenUtils;

    public CertificateService(ICertificateRepository certificateRepository, UserService userService, CertificateFileStorage certificateFileStorage, TokenUtils tokenUtils) {
        this.certificateRepository = certificateRepository;
        this.userService = userService;
        this.certificateFileStorage = certificateFileStorage;
        this.tokenUtils = tokenUtils;
    }

    @Override
    protected JpaRepository<Certificate, Integer> getEntityRepository() {
        return certificateRepository;
    }

    @Override
    public Certificate getBySerialNumber(String serialNumber) throws EntityNotFoundException{
        Optional<Certificate> certificate= Optional.ofNullable(certificateRepository.findBySerialNumber(serialNumber));
        if(certificate.isEmpty()) throw new EntityNotFoundException();
        else return certificate.get();
    }

    public X509Certificate generateCertificate(Certificate certificateRequest) throws ForbiddenException{
        try {
            JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");

            signerBuilder = signerBuilder.setProvider("BC");
            KeyPair keyPairSubject = generateKeyPair();

            ContentSigner contentSigner;

            X500Name issuerX500Name = null;
            if (certificateRequest.getIssuer()!=null){
                PrivateKey issuerPrivateKey=certificateFileStorage.getPrivateKeyFromStorage(certificateRequest.getIssuer());
                contentSigner = signerBuilder.build(issuerPrivateKey);

                Certificate issuerCert =  certificateRepository.findBySerialNumber(certificateRequest.getIssuer());
                if (issuerCert.getType().equals(CertificateType.END))
                    throw new ForbiddenException("End certificate can't be issuer");

                User issuer = userService.findByEmail(issuerCert.getEmail());
                X500NameBuilder builderIssuer = generateX500Name(issuer);
                builderIssuer.addRDN(BCStyle.UID, issuerCert.getSerialNumber());

                issuerX500Name = builderIssuer.build();
            }
            else{
//                PrivateKey issuerPrivateKey=certificateFileStorage.getPrivateKeyFromStorage(certificateRequest.getIssuer());
                contentSigner = signerBuilder.build(keyPairSubject.getPrivate());

                User issuer = userService.findByEmail(certificateRequest.getEmail());
                X500NameBuilder builderIssuer = generateX500Name(issuer);
                builderIssuer.addRDN(BCStyle.UID, certificateRequest.getSerialNumber());
                issuerX500Name = builderIssuer.build();
            }
            User subject = userService.findByEmail(certificateRequest.getEmail());
            X500NameBuilder builderSubject = generateX500Name(subject);


            String sn = certificateRequest.getSerialNumber();
            builderSubject.addRDN(BCStyle.UID, sn);

            setDateRangeForType(certificateRequest);

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
            addToKeystore("testKeystore.p12","password","testAlias",sn);
            return cert;
        } catch (IllegalArgumentException | IllegalStateException | OperatorCreationException | CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            System.out.println("mrs");
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("mrsssss");
            throw new RuntimeException(e);
        }
        return null;
    }

    private void setDateRangeForType(Certificate certificateRequest) {
        int years;
        switch (certificateRequest.getType()){
            case ROOT -> years=10;
            case INTERMEDIATE -> years = 3;
            default -> years = 1;
        }

        LocalDateTime now=LocalDateTime.now();
        certificateRequest.setStartDate(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        certificateRequest.setEndDate(Date.from(now.plusYears(years).atZone(ZoneId.systemDefault()).toInstant()));
    }

    @Override
    public Certificate createCertificateMetadata(RequestCreationDTO requestCreation){
        Certificate certificateMetaData = new Certificate(
                generateSerialNumber(),
                requestCreation.getSignatureAlgorithm(),
                requestCreation.getIssuer(),
                CertificateStatus.INVALID,
                requestCreation.getType(),
                requestCreation.getEmail(),
                "",
                false
        );
        return save(certificateMetaData);
    }

    @Override
    public byte[] getCertificatesInZip(String serialNumber, String authHeader)  throws EntityNotFoundException{
        Certificate certificate=getBySerialNumber(serialNumber);
        String token = authHeader.substring(7);
        File certFile = new File("src/main/resources/certificates/"+serialNumber+".crt");
        File keyFile = new File("src/main/resources/certificates/"+serialNumber+".key");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            addToZip(certFile, zipOutputStream, "certificate.crt");
            if (Objects.equals(tokenUtils.getEmailFromToken(token), certificate.getEmail()))
                addToZip(keyFile, zipOutputStream, "certificate.key");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private void addToZip(File file, ZipOutputStream zipOutputStream, String fileName) throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipEntry.setSize(file.length());
        zipOutputStream.putNextEntry(zipEntry);

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            zipOutputStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        zipOutputStream.closeEntry();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN"); //server.ssl.key-store-password key
            keyGen.initialize(1024, random);
            KeyPair keyPair=keyGen.generateKeyPair();
            return keyPair;
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
        return new BigInteger(sn.append(UUID.randomUUID()).append(UUID.randomUUID()).toString().replace("-", ""), 16).toString();
    }

    public String getOwnerOfCertificate(String serialNumber) {
        Certificate cert = certificateRepository.findBySerialNumber(serialNumber);
        return cert.getEmail();
    }
    public void addToKeystore(String keystorePath, String keystorePassword, String alias, String serialNumber) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        char[] password = keystorePassword.toCharArray();
        keystore.load(null, password);

        // Load the certificate
        X509Certificate certificate=certificateFileStorage.getCertificateFromStorage(serialNumber);

        // Load the private key
        PrivateKey privateKey=certificateFileStorage.getPrivateKeyFromStorage(serialNumber);

        // Create a keystore entry with the certificate and private key
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(privateKey,
                new java.security.cert.Certificate[]{certificate});
        KeyStore.ProtectionParameter passwordProtection = new KeyStore.PasswordProtection(password);
        keystore.setEntry(alias, privateKeyEntry, passwordProtection);

        // Save the keystore to a file
        FileOutputStream keystoreOutputStream = new FileOutputStream(keystorePath);
        keystore.store(keystoreOutputStream, password);

        // Close all the streams
        keystoreOutputStream.close();
    }
}
