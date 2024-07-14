package com.example.httploggingspringbootstarter.init;

import com.example.httploggingspringbootstarter.exception.HttpLoggingStartupException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class HttpLoggingFailureAnalyzer extends AbstractFailureAnalyzer<HttpLoggingStartupException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, HttpLoggingStartupException cause) {
        return new FailureAnalysis(cause.getMessage(), "Укажите валидные значения для свойства", cause);
    }
}
