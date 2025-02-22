package com.tyv.customerservice.exception;

public class CustomerNotFoundException extends DataNotFoundException {
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found, id=" + customerId);
    }
}
