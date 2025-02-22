package com.tyv.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tyv.customerservice.util.Formatter;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ExceptionDto {
    @JsonFormat(pattern = Formatter.DATE_TIME_PATTERN_DEV)
    private LocalDateTime timestamp;
    @JsonProperty("error_message")
    private String errorMessage;
}
