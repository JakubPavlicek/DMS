package com.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;

@SpringBootApplication
@EnableEnversRepositories
public class DocumentManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentManagerApplication.class, args);
    }
}
