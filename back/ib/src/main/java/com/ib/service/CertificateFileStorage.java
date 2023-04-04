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

}
