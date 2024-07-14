package com.example.httploggingspringbootstarter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogData {
    private String httpMethod;

    private String url;

    private List<HeaderData> requestHeaders;

    private String responseStatus;

    private Long executionTime;

    private List<HeaderData> responseHeaders;

    private String className;

    private String methodName;
}
