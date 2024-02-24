package com.dms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archive")
@Getter
public class ArchiveProperties {

    @Value("${archive.retention-period-days:60}")
    int retentionPeriodInDays;

}
