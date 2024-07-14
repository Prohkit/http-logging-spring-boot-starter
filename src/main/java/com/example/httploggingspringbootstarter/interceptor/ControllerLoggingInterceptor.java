package com.example.httploggingspringbootstarter.interceptor;

import com.example.httploggingspringbootstarter.model.LogData;
import com.example.httploggingspringbootstarter.service.LoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@AllArgsConstructor
@AutoConfigureAfter(LoggingService.class)
public class ControllerLoggingInterceptor implements HandlerInterceptor {

    private final LoggingService loggingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        if (handler instanceof HandlerMethod handlerMethod) {
            LogData logData = loggingService.getControllerLog(request, response, handlerMethod);
            loggingService.printControllerWelcomeMessage();
            loggingService.printLog(logData);
        }
    }
}