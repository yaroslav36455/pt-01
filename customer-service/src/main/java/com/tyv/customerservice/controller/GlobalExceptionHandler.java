package com.tyv.customerservice.controller;

import com.tyv.customerservice.dto.ExceptionDto;
import com.tyv.customerservice.exception.DataNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ExceptionDto> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return Mono.just(ExceptionDto.builder()
                        .timestamp(LocalDateTime.now())
                        .errorMessage("Duplicate key exception")
                        .build());
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<ExceptionDto>> handleNotFoundException(RuntimeException e) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(
                        ExceptionDto.builder()
                                .timestamp(LocalDateTime.now())
                                .errorMessage(e.getMessage())
                                .build()
                ));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ExceptionDto> handleBindException(WebExchangeBindException e) {
        List<String> fieldNames = e.getFieldErrors()
                .stream()
                .map(fieldError -> String.format("\"%s\":\"%s\"", fieldError.getField(), fieldError.getRejectedValue()))
                .toList();
        return Mono.just(ExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .errorMessage(String.format("Fields %s dose not match patterns", fieldNames))
                .build());
    }
}
