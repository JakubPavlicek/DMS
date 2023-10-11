package com.dms;

import com.dms.config.BlobStorageProperties;
import com.dms.config.HashProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@EnableConfigurationProperties({BlobStorageProperties.class, HashProperties.class})
public class DocumentManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagerApplication.class, args);
    }

}
