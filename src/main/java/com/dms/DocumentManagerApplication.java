package com.dms;

import com.dms.config.BlobStoragePropertiesValidator;
import com.dms.config.HashPropertiesValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BlobStoragePropertiesValidator.class, HashPropertiesValidator.class})
public class DocumentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagerApplication.class, args);
    }

}
