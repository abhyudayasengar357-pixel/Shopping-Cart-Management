package com.university.cartmanagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Main Spring Boot Application Entry Point for Online Shopping Cart and Order Management System.
 * Case Study Project: OOPs Using Java & Spring Boot / JDBC.
 */
@SpringBootApplication
public class OnlineCartApplication {

    private static final Logger log = LoggerFactory.getLogger(OnlineCartApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OnlineCartApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDefaultPasswords(com.university.cartmanagement.repository.UserRepository userRepository,
                                                  com.university.cartmanagement.util.PasswordUtil passwordUtil) {
        return args -> {
            try {
                userRepository.findByUsername("admin").ifPresent(user -> {
                    if (!passwordUtil.verifyPassword("admin123", user.getPassword())) {
                        userRepository.updatePassword(user.getId(), passwordUtil.hashPassword("admin123"));
                        log.info("Initialized secure BCrypt password for 'admin'");
                    }
                });
                userRepository.findByUsername("john_doe").ifPresent(user -> {
                    if (!passwordUtil.verifyPassword("password123", user.getPassword())) {
                        userRepository.updatePassword(user.getId(), passwordUtil.hashPassword("password123"));
                        log.info("Initialized secure BCrypt password for 'john_doe'");
                    }
                });
                userRepository.findByUsername("jane_smith").ifPresent(user -> {
                    if (!passwordUtil.verifyPassword("password123", user.getPassword())) {
                        userRepository.updatePassword(user.getId(), passwordUtil.hashPassword("password123"));
                        log.info("Initialized secure BCrypt password for 'jane_smith'");
                    }
                });
            } catch (Exception ex) {
                log.warn("Password initialization note: {}", ex.getMessage());
            }
        };
    }

    @Bean
    public CommandLineRunner verifyDatabaseConnection(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                log.info("====================================================================");
                log.info(" Online Shopping Cart & Order Management System successfully started!");
                log.info(" Connected Database: {} (v{})", conn.getMetaData().getDatabaseProductName(), conn.getMetaData().getDatabaseProductVersion());
                log.info(" JDBC Driver: {} (v{})", conn.getMetaData().getDriverName(), conn.getMetaData().getDriverVersion());
                log.info(" Application URL: http://localhost:8080");
                log.info(" Default Admin: admin / admin123");
                log.info(" Default Customer: john_doe / password123");
                log.info("====================================================================");
            } catch (Exception ex) {
                log.error("Failed to verify database connectivity on startup", ex);
            }
        };
    }
}
