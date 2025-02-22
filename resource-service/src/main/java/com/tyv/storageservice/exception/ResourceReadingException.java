package com.tyv.storageservice.exception;

public class ResourceReadingException extends ResourceException {
    public ResourceReadingException(String message) {
        super(message);
    }

  public ResourceReadingException(String message, Throwable cause) {
    super(message, cause);
  }
}
