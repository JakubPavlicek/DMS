package com.dms;

import com.dms.config.AdminUserProperties;
import com.dms.config.ArchiveProperties;
import com.dms.config.BlobStorageProperties;
import com.dms.config.HashProperties;
import com.dms.config.KeyProperties;
import com.dms.config.ServerProperties;
import com.dms.config.TokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Document Manager Application.
 * This class initializes and configures the Spring Boot application.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(
    {
        ServerProperties.class,
        BlobStorageProperties.class,
        HashProperties.class,
        ArchiveProperties.class,
        TokenProperties.class,
        KeyProperties.class,
        AdminUserProperties.class
    }
)
public class DocumentManagerApplication {

    /**
     * Main method to start the Document Manager Application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(DocumentManagerApplication.class, args);
    }

}
