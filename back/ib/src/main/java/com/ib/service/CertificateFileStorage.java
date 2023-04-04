package com.ib.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.security.PrivateKey;
import java.security.cert.*;

@Service
public class CertificateFileStorage {

    public void exportPrivateKey(PrivateKey privateKey, String serialNumber) throws IOException {
        File file = new File("/keys/" + serialNumber + ".key");
        FileOutputStream privateKeyFile = new FileOutputStream(file);
        privateKeyFile.write(privateKey.getEncoded());
        privateKeyFile.close();
    }

    public void exportCertificate(X509Certificate certificate) throws IOException, CertificateEncodingException {
        File file = new File("/certificates/" + certificate.getSerialNumber() + ".crt");
        FileOutputStream certificateFile = new FileOutputStream(file);
        certificateFile.write(certificate.getEncoded());
        certificateFile.write(certificate.getPublicKey().getEncoded());
        certificateFile.close();
    }

    public X509Certificate getCertificateFromStorage(String serialNumber){
        try{
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            FileInputStream is = new FileInputStream("\\path\\to\\file\\"+serialNumber+".crt");
            return (X509Certificate) fac.generateCertificate(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

    }
    public PrivateKey getPrivateKeyFromStorage(String serialNumber){
        try{
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            FileInputStream is = new FileInputStream("\\path\\to\\file\\"+serialNumber+".key");
            return (PrivateKey) fac.generateCertificate(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

    }

}
