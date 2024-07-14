package com.example.httploggingspringbootstarter.interceptor;

import com.example.httploggingspringbootstarter.TestController;
import com.example.httploggingspringbootstarter.configuration.HttpLoggingStarterAutoConfiguration;
import com.example.httploggingspringbootstarter.model.LogData;
import com.example.httploggingspringbootstarter.service.LoggingService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(OutputCaptureExtension.class)
@WireMockTest(httpPort = 8080)
public class RestClientLoggingInterceptorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpLoggingStarterAutoConfiguration.class));

    @Mock
    private LoggingService loggingServiceMock;

    @Test
    void loggingServiceInvokeMethodsWhenInterceptorIntercept() throws IOException {
        RestClientLoggingInterceptor interceptor = new RestClientLoggingInterceptor(loggingServiceMock);
        LogData logData = new LogData();
        HttpRequest request = new MockClientHttpRequest();
        ClientHttpResponse clientHttpResponse = mock(ClientHttpResponse.class);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        ClientHttpRequestExecution clientHttpRequestExecution = (request1, body) -> clientHttpResponse;

        when(loggingServiceMock.getRestClientLog(request, clientHttpResponse, null))
                .thenReturn(logData);

        interceptor.intercept(request, null, clientHttpRequestExecution);
        verify(loggingServiceMock)
                .getRestClientLog(any(HttpRequest.class), any(ClientHttpResponse.class), anyLong());
        verify(loggingServiceMock).printRestClientWelcomeMessage();
        verify(loggingServiceMock).printLog(null);

        clientHttpResponse.close();
    }

    @Test
    void returnsCorrectLogWhenLevelInfo(CapturedOutput capturedOutput) {
        contextRunner
                .withPropertyValues("http.logging.enabled=true",
                        "http.logging.level=INFO",
                        "http.logging.format=text")
                .run(context -> {
                    stubFor(get(urlEqualTo("/test"))
                            .willReturn(ok()));

                    RestClient restClient = context.getBean(RestClient.class);
                    TestController testController = new TestController(restClient);
                    testController.restClientRequest();
                    List<String> outputList = List.of(capturedOutput.getOut().split(System.lineSeparator()));
                    List<String> outputListOnlyLog = new ArrayList<>();
                    for (String output : outputList) {
                        outputListOnlyLog.add(output.split("-- ")[1]);
                    }
                    String replace = outputListOnlyLog.get(6).substring(0, 26) + "мс";
                    outputListOnlyLog.remove(6);
                    outputListOnlyLog.add(6, replace);

                    String secondReplace = outputListOnlyLog.get(7).substring(0, 17);
                    outputListOnlyLog.remove(7);
                    outputListOnlyLog.add(7, secondReplace);

                    String result = outputListOnlyLog.stream()
                            .map(log -> log + System.lineSeparator())
                            .collect(Collectors.joining());
                    String expected = "Логирование исходящего запроса" + System.lineSeparator() +
                            "-------------------------------" + System.lineSeparator() +
                            "Метод запроса: GET" + System.lineSeparator() +
                            "URL запроса: http://localhost:8080/test" + System.lineSeparator() +
                            "Заголовки запроса: [Content-Length:\"0\"]" + System.lineSeparator() +
                            "Код ответа: 200 OK" + System.lineSeparator() +
                            "Время выполнения запроса: мс" + System.lineSeparator() +
                            "Заголовки ответа:" + System.lineSeparator() +
                            "-------------------------------" + System.lineSeparator();

                    assertThat(result)
                            .isEqualTo(expected);
                });
    }

    @Test
    void returnsCorrectLogWhenLevelDebug(CapturedOutput capturedOutput) {
        contextRunner
                .withPropertyValues("http.logging.enabled=true",
                        "http.logging.level=DEBUG",
                        "http.logging.format=text")
                .run(context -> {
                    stubFor(get(urlEqualTo("/test"))
                            .willReturn(ok()));

                    RestClient restClient = context.getBean(RestClient.class);
                    TestController testController = new TestController(restClient);
                    testController.restClientRequest();
                    List<String> outputList = List.of(capturedOutput.getOut().split(System.lineSeparator()));
                    List<String> outputListOnlyLog = new ArrayList<>();
                    for (String output : outputList) {
                        String[] splittedOutput = output.split("-- ");
                        if (splittedOutput.length == 2) {
                            outputListOnlyLog.add(splittedOutput[1]);
                        } else {
                            outputListOnlyLog.add(output);
                        }
                    }
                    String replace = outputListOnlyLog.get(13).substring(0, 26) + "мс";
                    outputListOnlyLog.remove(13);
                    outputListOnlyLog.add(13, replace);
                    outputListOnlyLog.remove(17);
                    outputListOnlyLog.remove(16);
                    String result = outputListOnlyLog.stream()
                            .map(log -> log + System.lineSeparator())
                            .collect(Collectors.joining());
                    String expected = "Логирование исходящего запроса" + System.lineSeparator() +
                            "-------------------------------" + System.lineSeparator() +
                            "Логирование запроса:" + System.lineSeparator() +
                            "Имя класса: -" + System.lineSeparator() +
                            "Имя метода: -" + System.lineSeparator() +
                            "Метод запроса: GET" + System.lineSeparator() +
                            "URL запроса: http://localhost:8080/test" + System.lineSeparator() +
                            "Заголовки:" + System.lineSeparator() +
                            " Название заголовка = значение заголовка" + System.lineSeparator() +
                            "     Content-Length = 0" + System.lineSeparator() +
                            "" + System.lineSeparator() +
                            "Логирование ответа:" + System.lineSeparator() +
                            "Код ответа: 200 OK" + System.lineSeparator() +
                            "Время выполнения запроса: мс" + System.lineSeparator() +
                            "Заголовки:" + System.lineSeparator() +
                            " Название заголовка = значение заголовка";
                    assertThat(result.trim())
                            .isEqualTo(expected);
                });
    }

    @Test
    void returnsCorrectLogWhenLevelJson(CapturedOutput capturedOutput) {
        contextRunner
                .withPropertyValues("http.logging.enabled=true",
                        "http.logging.level=INFO",
                        "http.logging.format=json")
                .run(context -> {
                    stubFor(get(urlEqualTo("/test"))
                            .willReturn(ok()));

                    RestClient restClient = context.getBean(RestClient.class);
                    TestController testController = new TestController(restClient);
                    testController.restClientRequest();
                    String output = capturedOutput.getOut();
                    String result = output.substring(output.indexOf("{") - 1);
                    int startExecutionTimeIndex = result.indexOf("executionTime") + "executionTime".length() + 2;
                    String executionTime =
                            result.substring(startExecutionTimeIndex, result.indexOf("\"", startExecutionTimeIndex) - 1);
                    int startResponseHeadersIndex = result.indexOf("responseHeaders") + "responseHeaders".length() + 3;
                    result = result.substring(0, startResponseHeadersIndex)
                            + result.substring(result.indexOf("]", startResponseHeadersIndex));
                    String expected = "{" +
                            "\"httpMethod\":\"GET\"," +
                            "\"url\":\"http://localhost:8080/test\"," +
                            "\"requestHeaders\":[" +
                            "{" +
                            "\"headerName\":\"Content-Length\"," +
                            "\"headerValue\":\"0\"" +
                            "}" +
                            "]," +
                            "\"responseStatus\":\"200 OK\"," +
                            "\"executionTime\":" + executionTime + "," +
                            "\"responseHeaders\":[" +
                            "],\"className\":null," +
                            "\"methodName\":null" +
                            "}";

                    assertThat(result.trim())
                            .isEqualTo(expected);
                });
    }
}
