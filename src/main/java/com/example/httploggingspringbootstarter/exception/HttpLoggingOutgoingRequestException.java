package com.example.httploggingspringbootstarter.exception;

public class HttpLoggingOutgoingRequestException extends RuntimeException {
    public HttpLoggingOutgoingRequestException(String message) {
        super(message);
    }
}
