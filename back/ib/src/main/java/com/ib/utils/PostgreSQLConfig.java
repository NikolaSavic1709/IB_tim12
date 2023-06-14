package com.ib.utils;
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class PostgreSQLConfig {
//
//    @Value("${spring.datasource.url}")
//    private String jdbcUrl;
//
//    @Value("${spring.datasource.username}")
//    private String username;
//
//    @Value("${spring.datasource.password}")
//    private String password;
//
//    @Value("postgres_cert.crt")
//    private String sslCertPath;
//
//    @Value("postgres_cert.key")
//    private String sslKeyPath;
//
//    @Bean
//    public DataSource dataSource() {
//
////        HikariConfig config = new HikariConfig();
////        config.setJdbcUrl(jdbcUrl);
////        config.setUsername(username);
////        config.setPassword(password);
////        config.addDataSourceProperty("sslmode", "require");
////        config.addDataSourceProperty("sslcert", sslCertPath);
////        config.addDataSourceProperty("sslkey", sslKeyPath);
////
////        return new HikariDataSource(config);
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUrl(jdbcUrl);
////        dataSource.setUrl(jdbcUrl+"?sslmode=require&sslcert='postgres_cert.crt'&sslkey='postgres_cert.key'");
//        dataSource.setUsername(username);
//        dataSource.setPassword(password);
////        System.setProperty("javax.net.ssl.keyStore", sslKeyPath);
////        System.setProperty("javax.net.ssl.keyStorePassword", "password");
////        System.setProperty("javax.net.ssl.trustStore", sslCertPath);
////        System.setProperty("javax.net.ssl.trustStorePassword", "password");
//
//        return dataSource;
//    }
//}
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.security.KeyStore;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateFactory;
//import java.security.PrivateKey;
//import java.security.Key;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;
//import java.io.IOException;
//
//public class KeystoreUtils {
//
//    public static void addToKeystore(String keystorePath, String keystorePassword, String alias,
//                                     String certificatePath, String privateKeyPath, String privateKeyPassword) throws KeyStoreException,
//            NoSuchAlgorithmException, CertificateException, IOException {
//
//        KeyStore keystore = KeyStore.getInstance("PKCS12");
//        char[] password = keystorePassword.toCharArray();
//        keystore.load(null, password);
//
//        // Load the certificate
//        FileInputStream certificateInputStream = new FileInputStream(certificatePath);
//        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//        Certificate certificate = certificateFactory.generateCertificate(certificateInputStream);
//
//        // Load the private key
//        FileInputStream privateKeyInputStream = new FileInputStream(privateKeyPath);
//        Key privateKey = PrivateKeyReader.getPrivateKey(privateKeyInputStream, privateKeyPassword);
//
//        // Create a keystore entry with the certificate and private key
//        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry((PrivateKey) privateKey,
//                new Certificate[]{certificate});
//        KeyStore.ProtectionParameter passwordProtection = new KeyStore.PasswordProtection(password);
//        keystore.setEntry(alias, privateKeyEntry, passwordProtection);
//
//        // Save the keystore to a file
//        FileOutputStream keystoreOutputStream = new FileOutputStream(keystorePath);
//        keystore.store(keystoreOutputStream, password);
//
//        // Close all the streams
//        certificateInputStream.close();
//        privateKeyInputStream.close();
//        keystoreOutputStream.close();
//    }
//}

