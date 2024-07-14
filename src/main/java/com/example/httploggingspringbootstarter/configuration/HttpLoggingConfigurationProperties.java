package com.example.httploggingspringbootstarter.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "http.logging")
public class HttpLoggingConfigurationProperties {
    private boolean enabled;
    private String level;
    private String format;

    public boolean isDebug() {
        return level.equals("DEBUG");
    }

    public boolean isJsonFormat() {
        return format.equals("json");
    }

    public boolean isTextFormat() {
        return format.equals("text");
    }
}
