package com.example.httploggingspringbootstarter.exception.handler;

import com.example.httploggingspringbootstarter.exception.HttpLoggingOutgoingRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HttpLoggingExceptionHandler {

    @ExceptionHandler(HttpLoggingOutgoingRequestException.class)
    public ResponseEntity<String> httpLoggingOutgoingRequestException(HttpLoggingOutgoingRequestException exception) {
        return ResponseEntity.status(503).body(exception.getMessage());
    }
}
