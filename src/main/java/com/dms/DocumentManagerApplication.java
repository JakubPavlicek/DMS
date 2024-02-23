package com.dms;

import com.dms.config.BlobStorageProperties;
import com.dms.config.HashProperties;
import com.dms.config.KeyProperties;
import com.dms.config.SecurityUserProperties;
import com.dms.config.ServerProperties;
import com.dms.config.TokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ServerProperties.class, BlobStorageProperties.class, HashProperties.class, KeyProperties.class, SecurityUserProperties.class, TokenProperties.class})
public class DocumentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagerApplication.class, args);
    }

}
