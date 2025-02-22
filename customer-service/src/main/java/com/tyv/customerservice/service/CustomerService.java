package com.tyv.customerservice.service;

import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.dto.DocumentDto;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerDto> getCustomerById(Long id);
    Mono<CustomerDto> createCustomer(CustomerDto customerDto);
    Mono<CustomerDto> updateCustomer(CustomerDto customerDto);
    Mono<CustomerDto> updateCustomerDocument(CustomerDto customerDto);
    Mono<DocumentDto> getDocumentByCustomerId(Long id);
}
