package com.example.demonstration.controller;

import com.example.demonstration.configuration.DemonstrationConfigurationProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@AllArgsConstructor
public class TestController {
    private final RestClient restClient;

    private final DemonstrationConfigurationProperties properties;

    @GetMapping("/client")
    public ResponseEntity<Long> client() {
        Long response = restClient
                .get()
                .uri(properties.getUri())
                .retrieve()
                .toEntity(Long.class)
                .getBody();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<Long> test() {
        return ResponseEntity.ok(1L);
    }
}
