package com.example.httploggingspringbootstarter.interceptor;

import com.example.httploggingspringbootstarter.TestController;
import com.example.httploggingspringbootstarter.configuration.HttpLoggingStarterAutoConfiguration;
import com.example.httploggingspringbootstarter.model.LogData;
import com.example.httploggingspringbootstarter.service.LoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(OutputCaptureExtension.class)
class ControllerLoggingInterceptorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpLoggingStarterAutoConfiguration.class));

    @InjectMocks
    private TestController testController;

    private ControllerLoggingInterceptor interceptor;

    @Mock
    private LoggingService loggingServiceMock;

    @Test
    void startTimeAttributeIsNotNullWhenInterceptorPreHandle() {
        contextRunner
                .withPropertyValues("http.logging.enabled=true",
                        "http.logging.level=INFO",
                        "http.logging.format=text")
                .run(context -> {
                    LoggingService loggingServiceFromContext = context.getBean(LoggingService.class);
                    interceptor = new ControllerLoggingInterceptor(loggingServiceFromContext);

                    HttpServletRequest httpServletRequest = new MockHttpServletRequest();
                    HttpServletResponse httpServletResponse = new MockHttpServletResponse();
                    HandlerMethod handlerMethod = new HandlerMethod(testController, "test");

                    LogData logData = new LogData();
                    when(loggingServiceMock.getControllerLog(httpServletRequest, httpServletResponse, handlerMethod))
                            .thenReturn(logData);

                    assertThat(httpServletRequest.getAttribute("startTime")).isNull();
                    interceptor.preHandle(httpServletRequest, httpServletResponse, handlerMethod);
                    assertThat(httpServletRequest.getAttribute("startTime")).isNotNull();
                });
    }

    @Test
    void loggingServiceInvokeMethodsWhenInterceptorAfterCompletion() {
        contextRunner
                .withPropertyValues("http.logging.enabled=true",
                        "http.logging.level=INFO",
                        "http.logging.format=text")
                .run(context -> {
                    interceptor = new ControllerLoggingInterceptor(loggingServiceMock);

                    HttpServletRequest httpServletRequest = new MockHttpServletRequest();
                    HttpServletResponse httpServletResponse = new MockHttpServletResponse();
                    HandlerMethod handlerMethod = new HandlerMethod(testController, "test");

                    LogData logData = new LogData();
                    when(loggingServiceMock.getControllerLog(httpServletRequest, httpServletResponse, handlerMethod))
                            .thenReturn(logData);
                    interceptor.afterCompletion(httpServletRequest, httpServletResponse, handlerMethod, null);

                    verify(loggingServiceMock).getControllerLog(httpServletRequest, httpServletResponse, handlerMethod);
                    verify(loggingServiceMock).printControllerWelcomeMessage();
                    verify(loggingServiceMock).printLog(logData);
                });
    }

    @Test
    void returnsCorrectLogWhenLevelInfo(CapturedOutput capturedOutput) {
        contextRunner
                .withPropertyValues("http.logging.enabled=true",
                        "http.logging.level=INFO",
                        "http.logging.format=text")
                .run(context -> {
                    LoggingService loggingServiceFromContext = context.getBean(LoggingService.class);
                    interceptor = new ControllerLoggingInterceptor(loggingServiceFromContext);

                    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
                    httpServletRequest.setMethod("GET");
                    httpServletRequest.addHeader("requestHeader", 1);
                    MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
                    httpServletResponse.addHeader("responseHeader", String.valueOf(1));
                    HandlerMethod handlerMethod = new HandlerMethod(testController, "test");

                    interceptor.preHandle(httpServletRequest, httpServletResponse, handlerMethod);
                    interceptor.afterCompletion(httpServletRequest, httpServletResponse, handlerMethod, null);

                    List<String> outputList = List.of(capturedOutput.getOut().split(System.lineSeparator()));
                    List<String> outputListOnlyLog = new ArrayList<>();
                    for (String output : outputList) {
                        outputListOnlyLog.add(output.split("-- ")[1]);
                    }
                    String replace = outputListOnlyLog.get(6).substring(0, 26) + "мс";
                    outputListOnlyLog.remove(6);
                    outputListOnlyLog.add(6, replace);
                    String result = outputListOnlyLog.stream()
                            .map(log -> log + System.lineSeparator())
                            .collect(Collectors.joining());
                    String expected = "Логирование входящего запроса" + System.lineSeparator() +
                            "-------------------------------" + System.lineSeparator() +
                            "Метод запроса: GET" + System.lineSeparator() +
                            "URL запроса: http://localhost" + System.lineSeparator() +
                            "Заголовки запроса: [requestHeader:\"1\"]" + System.lineSeparator() +
                            "Код ответа: 200" + System.lineSeparator() +
                            "Время выполнения запроса: мс" + System.lineSeparator() +
                            "Заголовки ответа: [responseHeader:\"1\"]" + System.lineSeparator() +
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
                    LoggingService loggingServiceFromContext = context.getBean(LoggingService.class);
                    interceptor = new ControllerLoggingInterceptor(loggingServiceFromContext);

                    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
                    httpServletRequest.setMethod("GET");
                    httpServletRequest.addHeader("requestHeader", 1);
                    MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
                    httpServletResponse.addHeader("responseHeader", String.valueOf(1));
                    HandlerMethod handlerMethod = new HandlerMethod(testController, "test");

                    interceptor.preHandle(httpServletRequest, httpServletResponse, handlerMethod);
                    interceptor.afterCompletion(httpServletRequest, httpServletResponse, handlerMethod, null);

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
                    String result = outputListOnlyLog.stream()
                            .map(log -> log + System.lineSeparator())
                            .collect(Collectors.joining());
                    String expected = "Логирование входящего запроса" + System.lineSeparator() +
                            "-------------------------------" + System.lineSeparator() +
                            "Логирование запроса:" + System.lineSeparator() +
                            "Имя класса: TestController" + System.lineSeparator() +
                            "Имя метода: test" + System.lineSeparator() +
                            "Метод запроса: GET" + System.lineSeparator() +
                            "URL запроса: http://localhost" + System.lineSeparator() +
                            "Заголовки:" + System.lineSeparator() +
                            " Название заголовка = значение заголовка" + System.lineSeparator() +
                            "      requestHeader = 1" + System.lineSeparator() +
                            "" + System.lineSeparator() +
                            "Логирование ответа:" + System.lineSeparator() +
                            "Код ответа: 200" + System.lineSeparator() +
                            "Время выполнения запроса: мс" + System.lineSeparator() +
                            "Заголовки:" + System.lineSeparator() +
                            " Название заголовка = значение заголовка" + System.lineSeparator() +
                            "     responseHeader = 1" + System.lineSeparator();
                    assertThat(result)
                            .isEqualTo(expected);
                });
    }

    @Test
    void returnsCorrectLogWhenFormatJson(CapturedOutput capturedOutput) {
        contextRunner
                .withPropertyValues("http.logging.enabled=true",
                        "http.logging.level=INFO",
                        "http.logging.format=json")
                .run(context -> {
                    LoggingService loggingServiceFromContext = context.getBean(LoggingService.class);
                    interceptor = new ControllerLoggingInterceptor(loggingServiceFromContext);

                    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
                    httpServletRequest.setMethod("GET");
                    httpServletRequest.addHeader("requestHeader", 1);
                    MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
                    httpServletResponse.addHeader("responseHeader", String.valueOf(1));
                    HandlerMethod handlerMethod = new HandlerMethod(testController, "test");

                    interceptor.preHandle(httpServletRequest, httpServletResponse, handlerMethod);
                    interceptor.afterCompletion(httpServletRequest, httpServletResponse, handlerMethod, null);

                    String output = capturedOutput.getOut();
                    String result = output.substring(output.indexOf("{") - 1);
                    char executionTime = result.charAt(153);
                    String expected = "{" +
                            "\"httpMethod\":\"GET\"," +
                            "\"url\":\"http://localhost\"," +
                            "\"requestHeaders\":[" +
                            "{" +
                            "\"headerName\":\"requestHeader\"," +
                            "\"headerValue\":\"1\"" +
                            "}" +
                            "]," +
                            "\"responseStatus\":\"200\"," +
                            "\"executionTime\":" + executionTime + "," +
                            "\"responseHeaders\":[" +
                            "{" +
                            "\"headerName\":\"responseHeader\"," +
                            "\"headerValue\":\"1\"" +
                            "}" +
                            "]," +
                            "\"className\":null," +
                            "\"methodName\":null" +
                            "}";

                    assertThat(result.trim())
                            .isEqualTo(expected);
                });
    }
}