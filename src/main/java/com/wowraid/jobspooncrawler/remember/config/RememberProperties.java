package com.wowraid.jobspooncrawler.remember.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter @Setter
@Validated
@ConfigurationProperties(prefix = "crawler.remember")
public class RememberProperties {

    @NotBlank
    private String baseUrl;        // <-- camelCase

    private long waitMillis;

    private String listSelector;
}