package com.tyv.customerservice.repository;

import com.tyv.customerservice.entity.Customer;
import com.tyv.customerservice.entity.CustomersDocument;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerRepository {

    Mono<Customer> findById(Long id);
    Mono<Customer> save(Customer customer);
    Mono<CustomersDocument> getDocumentById(Long id);
    Mono<Long> updateCustomerDocument(Long id, UUID document);
}
