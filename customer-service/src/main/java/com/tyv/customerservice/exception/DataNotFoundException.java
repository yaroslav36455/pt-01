package com.tyv.customerservice.exception;

public abstract class DataNotFoundException extends RuntimeException {
  public DataNotFoundException(String message) {
    super(message);
  }

  public DataNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
