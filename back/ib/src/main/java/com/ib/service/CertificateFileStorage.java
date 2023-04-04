package com.ib.service;

import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@Service
public class CertificateFileStorage {

    public void exportPrivateKey(PrivateKey privateKey, String serialNumber) throws IOException {
        FileOutputStream privateKeyFile = new FileOutputStream(serialNumber + ".key");
        privateKeyFile.write(privateKey.getEncoded());
        privateKeyFile.close();
    }

    public void exportCertificate(X509Certificate certificate) throws IOException, CertificateEncodingException {
        FileOutputStream certificateFile = new FileOutputStream(certificate.getSerialNumber() + ".crt");
        certificateFile.write(certificate.getEncoded());
        certificateFile.write(certificate.getPublicKey().getEncoded());
        certificateFile.close();
    }
}
