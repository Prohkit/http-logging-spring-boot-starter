package com.example.demonstration.configuration;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "client")
public class DemonstrationConfigurationProperties {
    private String uri;
}
