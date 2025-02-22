package com.tyv.customerservice.exception;

import com.tyv.customerservice.dto.ExceptionDto;
import lombok.Getter;

import java.net.URI;

@Getter
public class DocumentResourceConsistencyException extends RuntimeException {
    private final ExceptionDto responseExceptionDto;

    public DocumentResourceConsistencyException(URI uri, ExceptionDto dto) {
      super("Document resource was not found due to request [" + uri + ']');
      this.responseExceptionDto = dto;
    }
}
