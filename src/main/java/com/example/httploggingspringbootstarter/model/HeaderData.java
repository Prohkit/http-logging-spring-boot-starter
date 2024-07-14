package com.example.httploggingspringbootstarter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeaderData {
    private String headerName;

    private String headerValue;
}
