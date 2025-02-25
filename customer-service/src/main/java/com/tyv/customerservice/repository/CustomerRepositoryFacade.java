package com.tyv.customerservice.repository;

import com.tyv.customerservice.entity.Customer;
import com.tyv.customerservice.entity.CustomersDocument;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerRepositoryFacade implements CustomerRepository {
    CustomerRepositoryCrud customerRepository;
    DatabaseClient databaseClient;

    @Override
    public Mono<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public Mono<Customer> save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Mono<CustomersDocument> getDocumentById(Long id) {
        return customerRepository.getDocumentById(id);
    }

    @Override
    public Mono<Long> updateCustomerDocument(Long id, UUID document) {
        return databaseClient.sql("UPDATE customer c SET document=:document WHERE id=:id")
                .bind("document", document)
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }
}
