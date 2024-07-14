package com.example.demonstration.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
