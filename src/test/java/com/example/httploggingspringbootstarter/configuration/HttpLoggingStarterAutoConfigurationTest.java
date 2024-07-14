package com.example.httploggingspringbootstarter.configuration;

import com.example.httploggingspringbootstarter.exception.HttpLoggingStartupException;
import com.example.httploggingspringbootstarter.init.HttpLoggingEnvironmentPostProcessor;
import com.example.httploggingspringbootstarter.interceptor.ControllerLoggingInterceptor;
import com.example.httploggingspringbootstarter.interceptor.RestClientLoggingInterceptor;
import com.example.httploggingspringbootstarter.service.LoggingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class HttpLoggingStarterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpLoggingStarterAutoConfiguration.class));

    @Test
    void controllerLoggingInterceptorIsRegisteredWhenEnabledPropertyIsTrue() {
        contextRunner
                .withPropertyValues("http.logging.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoggingService.class);
                    assertThat(context).hasSingleBean(ControllerLoggingInterceptor.class);
                    assertThat(context).hasSingleBean(ControllerLoggingConfiguration.class);

                    ControllerLoggingConfiguration loggingConfiguration
                            = context.getBean(ControllerLoggingConfiguration.class);
                    InterceptorRegistry registry = mock(InterceptorRegistry.class);
                    loggingConfiguration.addInterceptors(registry);
                    verify(registry).addInterceptor(any(ControllerLoggingInterceptor.class));
                });
    }

    @Test
    void restClientLoggingInterceptorIsRegisteredWhenEnabledPropertyIsTrue() {
        contextRunner
                .withPropertyValues("http.logging.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoggingService.class);
                    assertThat(context).hasSingleBean(RestClientLoggingInterceptor.class);
                    assertThat(context).hasSingleBean(RestClient.class);

                    RestClient restClient = context.getBean(RestClient.class);
                    List<RestClientLoggingInterceptor> interceptorList =
                            (List<RestClientLoggingInterceptor>) ReflectionTestUtils.getField(restClient, "interceptors");

                    assertThat(interceptorList).hasSize(1);
                });
    }

    @Test
    void controllerLoggingInterceptorIsNotRegisteredWhenEnabledPropertyIsFalse() {
        contextRunner
                .withPropertyValues("http.logging.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LoggingService.class);
                    assertThat(context).doesNotHaveBean(ControllerLoggingInterceptor.class);
                    assertThat(context).doesNotHaveBean(ControllerLoggingConfiguration.class);
                });
    }

    @Test
    void restClientLoggingInterceptorIsNotRegisteredWhenEnabledPropertyIsFalse() {
        contextRunner
                .withPropertyValues("http.logging.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LoggingService.class);
                    assertThat(context).doesNotHaveBean(RestClientLoggingInterceptor.class);
                    assertThat(context).doesNotHaveBean(RestClient.class);
                });
    }

    @Test
    void throwHttpLoggingStartupExceptionWhenInvalidProperty() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources()
                .addFirst(new MapPropertySource("test", Collections.singletonMap("http.logging.enabled", "invalid")));

        HttpLoggingEnvironmentPostProcessor postProcessor = new HttpLoggingEnvironmentPostProcessor();
        assertThatExceptionOfType(HttpLoggingStartupException.class)
                .isThrownBy(() -> postProcessor.postProcessEnvironment(environment, new SpringApplication()))
                .withMessageContaining("Недопустимые значения параметра http.logging.enabled. Возможные значения: true, false.");
    }
}