package com.example.httploggingspringbootstarter;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@AllArgsConstructor
public class TestController {

    private final RestClient restClient;

    @GetMapping("/test")
    public ResponseEntity<Long> test() {
        return ResponseEntity.ok(1L);
    }

    @GetMapping("/restClient")
    public ResponseEntity<Long> restClientRequest() {
        Long response = restClient
                .get()
                .uri("http://localhost:8080/test")
                .retrieve()
                .toEntity(Long.class)
                .getBody();
        return ResponseEntity.ok(response);
    }
}
