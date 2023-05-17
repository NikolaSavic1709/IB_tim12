package com.ib.utils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class PostgreSQLConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("postgres_cert.crt")
    private String sslCertPath;

    @Value("postgres_cert.key")
    private String sslKeyPath;

    @Bean
    public DataSource dataSource() {

//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(jdbcUrl);
//        config.setUsername(username);
//        config.setPassword(password);
//        config.addDataSourceProperty("sslmode", "require");
//        config.addDataSourceProperty("sslcert", sslCertPath);
//        config.addDataSourceProperty("sslkey", sslKeyPath);
//
//        return new HikariDataSource(config);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        System.setProperty("javax.net.ssl.keyStore", sslKeyPath);
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", sslCertPath);
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        return dataSource;
    }
}
