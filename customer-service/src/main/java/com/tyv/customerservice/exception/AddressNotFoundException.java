package com.tyv.customerservice.exception;

public class AddressNotFoundException extends DataNotFoundException {
    public AddressNotFoundException(Long addressId) {
        super("Address not found, id=" + addressId);
    }
}
