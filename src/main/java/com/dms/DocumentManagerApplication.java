package com.dms;

import com.dms.config.ArchiveProperties;
import com.dms.config.BlobStorageProperties;
import com.dms.config.HashProperties;
import com.dms.config.KeyProperties;
import com.dms.config.SecurityUserProperties;
import com.dms.config.ServerProperties;
import com.dms.config.TokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

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
        SecurityUserProperties.class
    }
)
public class DocumentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagerApplication.class, args);
    }

}
