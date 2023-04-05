package com.ib.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Service
public class CertificateFileStorage {

    public void exportPrivateKey(PrivateKey privateKey, String serialNumber) throws IOException {

        File file = new File("src/main/resources/certificates/" + serialNumber + ".key");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream privateKeyFile = new FileOutputStream(file);
        privateKeyFile.write(privateKey.getEncoded());
        privateKeyFile.close();
    }

    public void exportCertificate(X509Certificate certificate) throws IOException, CertificateEncodingException {
        File file = new File("src/main/resources/certificates/" + certificate.getSerialNumber() + ".crt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream certificateFile = new FileOutputStream(file);
        certificateFile.write(certificate.getEncoded());
        certificateFile.write(certificate.getPublicKey().getEncoded());
        certificateFile.close();
    }

    public X509Certificate getCertificateFromStorage(String serialNumber){
        try{
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            FileInputStream is = new FileInputStream("src/main/resources/certificates/"+serialNumber+".crt");
            return (X509Certificate) fac.generateCertificate(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

    }
    public PrivateKey getPrivateKeyFromStorage(String serialNumber){
        try{
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get("src/main/resources/certificates/"+serialNumber+".key"));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(spec);
//            CertificateFactory fac = CertificateFactory.getInstance("X509");
//            FileInputStream is = new FileInputStream("src/main/resources/certificates/"+serialNumber+".key");
//            return (PrivateKey) fac.generate(is);
            return privateKey;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

}
