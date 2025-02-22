package com.tyv.productservice.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Data
public class ErrorResponseDto {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String message;
}
