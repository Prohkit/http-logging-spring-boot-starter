package com.example.httploggingspringbootstarter.configuration;

import com.example.httploggingspringbootstarter.interceptor.ControllerLoggingInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
@AutoConfigureAfter(ControllerLoggingInterceptor.class)
@ConditionalOnProperty(prefix = "http.logging", value = "enabled", havingValue = "true")
public class ControllerLoggingConfiguration implements WebMvcConfigurer {

    private final ControllerLoggingInterceptor controllerLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerLoggingInterceptor);
    }
}
