package com.tyv.customerservice.repository;

import com.tyv.customerservice.entity.Customer;
import com.tyv.customerservice.entity.CustomersDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepositoryCrud extends ReactiveCrudRepository<Customer, Long> {
    Mono<CustomersDocument> getDocumentById(Long id);
}
