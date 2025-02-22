package com.tyv.customerservice.exception;


public class DocumentNotFoundException extends DataNotFoundException {
    public DocumentNotFoundException(Long customerId) {
        super(buildMessage(customerId));
    }

    public DocumentNotFoundException(Long customerId, Throwable cause) {
        super(buildMessage(customerId), cause);
    }

    static private String buildMessage(Long customerId) {
        return "Document was not found for customer, id=" + customerId;
    }
}
