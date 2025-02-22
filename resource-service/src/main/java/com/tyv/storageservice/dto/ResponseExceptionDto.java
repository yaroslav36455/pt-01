package com.tyv.storageservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class ResponseExceptionDto {
    private LocalDateTime timestamp;
    @JsonProperty("error_message")
    private String errorMessage;
}
