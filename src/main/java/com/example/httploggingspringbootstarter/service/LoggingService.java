package com.example.httploggingspringbootstarter.service;

import com.example.httploggingspringbootstarter.configuration.HttpLoggingConfigurationProperties;
import com.example.httploggingspringbootstarter.model.HeaderData;
import com.example.httploggingspringbootstarter.model.LogData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;
import java.util.*;

@Slf4j(topic = "http logging")
@AllArgsConstructor
@AutoConfigureAfter({HttpLoggingConfigurationProperties.class, ObjectMapper.class})
public class LoggingService {
    private final HttpLoggingConfigurationProperties logConfig;

    private final ObjectMapper objectMapper;

    private final String firstLineInHeaderOutput = "Название заголовка = значение заголовка";

    public void printLog(LogData logData) {
        if (logConfig.isJsonFormat()) {
            String jsonLogData = getLogDataJson(logData);
            log.info(jsonLogData);
        } else if (logConfig.isTextFormat()) {
            if (!logConfig.isDebug()) {
                printInfoLog(logData);
            } else if (logConfig.isDebug()) {
                printDebugLog(logData);
            }
        }
    }

    public void printControllerWelcomeMessage() {
        log.info("Логирование входящего запроса");
    }

    public void printRestClientWelcomeMessage() {
        log.info("Логирование исходящего запроса");
    }

    public LogData getControllerLog(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        LogData logData = getGeneralDataController(request, response);
        List<HeaderData> requestHeaders = getRequestHeaderData(request);
        List<HeaderData> responseHeaders = getResponseHeaderData(response);
        if (logConfig.isDebug()) {
            String className = handlerMethod.getBean().getClass().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            logData.setClassName(className);
            logData.setMethodName(methodName);
        }
        logData.setRequestHeaders(requestHeaders);
        logData.setResponseHeaders(responseHeaders);
        return logData;
    }

    public LogData getRestClientLog(HttpRequest request, ClientHttpResponse clientHttpResponse, Long executionTime) {
        LogData logData = getGeneralDataRestClient(request, clientHttpResponse, executionTime);
        List<HeaderData> requestHeaders = getRequestHeaderData(request);
        List<HeaderData> responseHeaders = getResponseHeaderData(clientHttpResponse);
        if (logConfig.isDebug()) {
            logData.setClassName("-");
            logData.setMethodName("-");
        }
        logData.setRequestHeaders(requestHeaders);
        logData.setResponseHeaders(responseHeaders);
        return logData;
    }

    private void printInfoLog(LogData logData) {
        log.info("-------------------------------");
        log.info("Метод запроса: {}", logData.getHttpMethod());
        log.info("URL запроса: {}", logData.getUrl());
        log.info(getHeadersInfo(logData.getRequestHeaders(), "Заголовки запроса"));

        log.info("Код ответа: {}", logData.getResponseStatus());
        log.info("Время выполнения запроса: {} мс", logData.getExecutionTime());
        log.info(getHeadersInfo(logData.getResponseHeaders(), "Заголовки ответа"));
        log.info("-------------------------------");
    }

    private void printDebugLog(LogData logData) {
        log.info("-------------------------------");
        log.info("Логирование запроса:");
        log.info("Имя класса: {}", logData.getClassName());
        log.info("Имя метода: {}", logData.getMethodName());
        log.info("Метод запроса: {}", logData.getHttpMethod());
        log.info("URL запроса: {}", logData.getUrl());
        log.info(getHeadersDebug(logData.getRequestHeaders()));

        log.info("Логирование ответа:");
        log.info("Код ответа: {}", logData.getResponseStatus());
        log.info("Время выполнения запроса: {} мс", logData.getExecutionTime());
        log.info(getHeadersDebug(logData.getResponseHeaders()));
    }

    private String getLogDataJson(LogData logData) {
        try {
            return objectMapper.writeValueAsString(logData);
        } catch (JsonProcessingException exception) {
            log.info("Ошибка при сериализации логов в json");
        }
        return null;
    }

    private String getHeadersInfo(List<HeaderData> headerDataList, String headerString) {
        StringBuilder sb = new StringBuilder(headerString).append(": [");
        List<String> headerNames = headerDataList.stream().map(HeaderData::getHeaderName).toList();
        for (int i = 0; i < headerNames.size(); i++) {
            String headerName = headerNames.get(i);
            sb.append(headerName).append(":\"").append(headerDataList.get(i).getHeaderValue());
            if (i != headerNames.size() - 1) {
                sb.append("\", ");
            }
        }
        sb.append("\"]");
        return sb.toString();
    }

    private List<HeaderData> getRequestHeaderData(HttpServletRequest request) {
        Iterator<String> headerIterator = request.getHeaderNames().asIterator();
        ArrayList<HeaderData> requestHeaderDataList = new ArrayList<>();
        while (headerIterator.hasNext()) {
            String headerName = headerIterator.next();
            String headerValue = request.getHeader(headerName);
            requestHeaderDataList.add(new HeaderData(headerName, headerValue));
        }
        return requestHeaderDataList;
    }

    private List<HeaderData> getResponseHeaderData(HttpServletResponse response) {
        List<String> responseHeaderNames = response.getHeaderNames().stream().toList();
        List<HeaderData> responseHeaderDataList = new ArrayList<>();
        for (String headerName : responseHeaderNames) {
            String headerValue = response.getHeader(headerName);
            responseHeaderDataList.add(new HeaderData(headerName, headerValue));
        }
        return responseHeaderDataList;
    }

    private List<HeaderData> getRequestHeaderData(HttpRequest request) {
        Set<Map.Entry<String, List<String>>> headersEntrySet = request.getHeaders().entrySet();
        return getHeaderData(headersEntrySet);
    }

    private List<HeaderData> getResponseHeaderData(ClientHttpResponse clientHttpResponse) {
        Set<Map.Entry<String, List<String>>> headersEntrySet = clientHttpResponse.getHeaders().entrySet();
        return getHeaderData(headersEntrySet);
    }

    private List<HeaderData> getHeaderData(Set<Map.Entry<String, List<String>>> headersEntrySet) {
        List<HeaderData> headerDataList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headersEntrySet) {
            String headerName = entry.getKey();
            List<String> headerValues = entry.getValue();
            for (String headerValue : headerValues) {
                headerDataList.add(new HeaderData(headerName, headerValue));
            }
        }
        return headerDataList;
    }

    private LogData getGeneralDataController(HttpServletRequest request, HttpServletResponse response) {
        Long startTime = (Long) request.getAttribute("startTime");
        String httpMethod = request.getMethod();
        String url = request.getRequestURL().toString();
        String httpStatus = String.valueOf(response.getStatus());
        Long executionTime = System.currentTimeMillis() - startTime;
        return LogData.builder().httpMethod(httpMethod).url(url).responseStatus(httpStatus).executionTime(executionTime).build();
    }

    private LogData getGeneralDataRestClient(HttpRequest request, ClientHttpResponse clientHttpResponse, Long executionTime) {
        String httpMethod = request.getMethod().name();
        String url = request.getURI().toString();
        String httpStatus = null;
        try {
            httpStatus = clientHttpResponse.getStatusCode().toString();
        } catch (IOException e) {
            log.error("Ошибка при получении кода ответа.");
        }
        return LogData.builder().httpMethod(httpMethod).url(url).responseStatus(httpStatus).executionTime(executionTime).build();
    }

    private Integer getLongestHeaderNameLength(List<String> requestHeaderNames) {
        int longestHeaderNameLength = firstLineInHeaderOutput.indexOf("=");
        for (String headerName : requestHeaderNames) {
            int headerNameLength = headerName.length();
            if (headerNameLength > longestHeaderNameLength) {
                longestHeaderNameLength = headerNameLength;
            }
        }
        return longestHeaderNameLength;
    }

    private String getHeadersDebug(List<HeaderData> headerDataList) {
        List<String> requestHeaderNames = headerDataList.stream().map(HeaderData::getHeaderName).toList();
        int longestHeaderNameLength = getLongestHeaderNameLength(requestHeaderNames);

        String requestHeadersName = "Заголовки:";
        String headerFirstAndSecondLines = getHeaderFirstAndSecondLinesDebug(requestHeadersName, longestHeaderNameLength);
        StringBuilder sb = new StringBuilder(headerFirstAndSecondLines);
        for (HeaderData headerData : headerDataList) {
            String headerOutput = getHeaderOutputDebug(headerData.getHeaderValue(), headerData.getHeaderName(), longestHeaderNameLength);
            sb.append(headerOutput);
        }
        return sb.toString();
    }

    private String getHeaderOutputDebug(String header, String headerName, int longestHeaderNameLength) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(longestHeaderNameLength - headerName.length())).append(headerName).append(" = ").append(header).append(System.lineSeparator());
        return sb.toString();
    }

    private String getHeaderFirstAndSecondLinesDebug(String headersName, int longestHeaderNameLength) {
        String firstLineInHeaderOutput = getCenteredFirstLineInHeader(longestHeaderNameLength);
        StringBuilder sb = new StringBuilder(headersName).append(System.lineSeparator()).append(firstLineInHeaderOutput).append(System.lineSeparator());
        return sb.toString();
    }

    private String getCenteredFirstLineInHeader(Integer longestHeaderNameLength) {
        if (firstLineInHeaderOutput.indexOf("=") <= longestHeaderNameLength) {
            int spacesToCenter = longestHeaderNameLength - firstLineInHeaderOutput.indexOf("=") + 1;
            return " ".repeat(spacesToCenter) + firstLineInHeaderOutput;
        }
        return firstLineInHeaderOutput;
    }
}
