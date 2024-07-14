package com.example.httploggingspringbootstarter.init;

import com.example.httploggingspringbootstarter.exception.HttpLoggingStartupException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class HttpLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final PropertiesPropertySourceLoader propertySourceLoader;

    public HttpLoggingEnvironmentPostProcessor() {
        propertySourceLoader = new PropertiesPropertySourceLoader();
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var resource = new ClassPathResource("default.properties");
        PropertySource<?> propertySource;
        try {
            propertySource = propertySourceLoader.load("http.logging", resource).get(0);
        } catch (IOException e) {
            throw new HttpLoggingStartupException("Ошибка при загрузке настроек по умолчанию http logging стартера.");
        }
        environment.getPropertySources().addLast(propertySource);
        String enabledPropertyValue = environment.getProperty("http.logging.enabled");
        boolean isBoolValue = Boolean.TRUE.toString().equals(enabledPropertyValue)
                || Boolean.FALSE.toString().equals(enabledPropertyValue);

        if (!isBoolValue) {
            throw new HttpLoggingStartupException("Недопустимые значения параметра http.logging.enabled. Возможные значения: true, false.");
        }
    }
}
