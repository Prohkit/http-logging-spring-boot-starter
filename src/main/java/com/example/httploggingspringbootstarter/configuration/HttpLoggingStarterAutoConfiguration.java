package com.example.httploggingspringbootstarter.configuration;

import com.example.httploggingspringbootstarter.interceptor.ControllerLoggingInterceptor;
import com.example.httploggingspringbootstarter.interceptor.RestClientLoggingInterceptor;
import com.example.httploggingspringbootstarter.service.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableConfigurationProperties(HttpLoggingConfigurationProperties.class)
@ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
public class HttpLoggingStarterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
    public HttpLoggingConfigurationProperties configurationProperties() {
        return new HttpLoggingConfigurationProperties();
    }

    @Bean
    @DependsOn("configurationProperties")
    @ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
    public LoggingService loggingService(HttpLoggingConfigurationProperties configurationProperties,
                                         ObjectMapper objectMapper) {
        return new LoggingService(configurationProperties, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
    public ControllerLoggingInterceptor controllerLoggingInterceptor(LoggingService loggingService) {
        return new ControllerLoggingInterceptor(loggingService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
    public RestClientLoggingInterceptor restClientLoggingInterceptor(LoggingService loggingService) {
        return new RestClientLoggingInterceptor(loggingService);
    }

    @Bean
    @DependsOn("controllerLoggingInterceptor")
    @ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
    public ControllerLoggingConfiguration controllerLoggingConfiguration(ControllerLoggingInterceptor interceptor) {
        return new ControllerLoggingConfiguration(interceptor);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
    public RestClient restClientHttpLoggingStarter(RestClientLoggingInterceptor restClientLoggingInterceptor) {
        return RestClient.builder().requestInterceptor(restClientLoggingInterceptor).build();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapperHttpLoggingStarter() {
        return new ObjectMapper();
    }
}
