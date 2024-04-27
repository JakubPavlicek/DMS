package com.dms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for archiving documents, defined application.yaml.
 * Properties are prefixed with "archive".
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@ConfigurationProperties(prefix = "archive")
@Getter
public class ArchiveProperties {

    /**
     * Retention period for archived documents in days.
     * Default value is 60.
     */
    @Value("${archive.retention-period-days:60}")
    int retentionPeriodInDays;

}
