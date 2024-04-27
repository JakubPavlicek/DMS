package com.dms.config;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.configuration.SpringDocUIConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Configuration class for SpringDoc settings.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Configuration
public class SpringDocConfig {

    /**
     * Constructor to add application/octet-stream media type to support file uploading via swagger-ui.
     *
     * @param converter the Jackson {@link MappingJackson2HttpMessageConverter} bean
     */
    public SpringDocConfig(MappingJackson2HttpMessageConverter converter) {
        ArrayList<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }

    /**
     * Bean for {@link SpringDocConfiguration}.
     *
     * @return an instance of {@link SpringDocConfiguration}
     */
    @Bean
    SpringDocConfiguration springDocConfiguration() {
        return new SpringDocConfiguration();
    }

    /**
     * Bean for {@link SpringDocConfigProperties}.
     *
     * @return an instance of {@link SpringDocConfigProperties}
     */
    @Bean
    SpringDocConfigProperties springDocConfigProperties() {
        return new SpringDocConfigProperties();
    }

    /**
     * Bean for {@link ObjectMapperProvider}.
     *
     * @param springDocConfigProperties the configuration properties for SpringDoc
     * @return an instance of {@link ObjectMapperProvider}
     */
    @Bean
    ObjectMapperProvider objectMapperProvider(SpringDocConfigProperties springDocConfigProperties) {
        return new ObjectMapperProvider(springDocConfigProperties);
    }

    /**
     * Bean for {@link SpringDocUIConfiguration}.
     *
     * @param optionalSwaggerUiConfigProperties optional configuration properties for Swagger UI
     * @return an instance of {@link SpringDocUIConfiguration}
     */
    @Bean
    SpringDocUIConfiguration springDocUIConfiguration(Optional<SwaggerUiConfigProperties> optionalSwaggerUiConfigProperties) {
        return new SpringDocUIConfiguration(optionalSwaggerUiConfigProperties);
    }

}