package com.example.httploggingspringbootstarter.interceptor;

import com.example.httploggingspringbootstarter.exception.HttpLoggingOutgoingRequestException;
import com.example.httploggingspringbootstarter.model.LogData;
import com.example.httploggingspringbootstarter.service.LoggingService;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@AllArgsConstructor
@AutoConfigureAfter(LoggingService.class)
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final LoggingService loggingService;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
        try {
            long startTime = System.currentTimeMillis();
            ClientHttpResponse clientHttpResponse = execution.execute(request, body);
            long executionTime = System.currentTimeMillis() - startTime;

            LogData logData = loggingService.getRestClientLog(request, clientHttpResponse, executionTime);
            loggingService.printRestClientWelcomeMessage();
            loggingService.printLog(logData);
            return clientHttpResponse;
        } catch (IOException e) {
            throw new HttpLoggingOutgoingRequestException("Ошибка при выполнении исходящего запроса");
        }
    }
}
