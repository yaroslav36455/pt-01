package com.tyv.customerservice.service;

import com.tyv.customerservice.client.DocumentClient;
import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.dto.DocumentDto;
import com.tyv.customerservice.exception.CustomerNotFoundException;
import com.tyv.customerservice.exception.DocumentNotFoundException;
import com.tyv.customerservice.exception.DocumentResourceConsistencyException;
import com.tyv.customerservice.mapper.CustomerMapper;
import com.tyv.customerservice.repository.AddressRepository;
import com.tyv.customerservice.repository.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    DocumentClient documentClient;
    AddressRepository addressRepository;

    @Override
    @Transactional
    public Mono<CustomerDto> getCustomerById(Long id) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                .flatMap(customer -> addressRepository.getAddressByCustomerId(id)
                        .map(address -> {
                            customer.setAddress(address);
                            return customer;
                        })
                        .defaultIfEmpty(customer))
                .map(customerMapper::toDto)
                .doOnError(CustomerNotFoundException.class, exception -> log.warn(exception.toString()));
    }

    @Override
    public Mono<CustomerDto> createCustomer(CustomerDto customerDto) {
        return Mono.just(customerDto)
                .map(customerMapper::toEntity)
                .flatMap(customerRepository::save)
                .doOnSuccess(customer -> log.info("Customer {} created", customer.getId()))
                .map(customerMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<CustomerDto> updateCustomer(CustomerDto customerDto) {
        return customerRepository.findById(customerDto.getId())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerDto.getId())))
                .map(customer -> {
                    customerMapper.toEntityUpdate(customer, customerDto);
                    return customer;
                })
                .flatMap(customerRepository::save)
                .flatMap(customer -> addressRepository.getAddressByCustomerId(customer.getId())
                        .map(address -> {
                            customer.setAddress(address);
                            return customer;
                        }))
                .doOnSuccess(customer -> log.info("Customer id={} updated", customer.getId()))
                .onErrorMap(NoSuchElementException.class, e -> new CustomerNotFoundException(customerDto.getId()))
                .doOnError(CustomerNotFoundException.class, customer -> log.warn(customer.toString()))
                .map(customerMapper::toDto);
    }

    @Override
    public Mono<CustomerDto> updateCustomerDocument(CustomerDto customerDto) {
        return customerRepository.updateCustomerDocument(customerDto.getId(), customerDto.getDocument())
                .doOnError(exception -> log.error("Customer's document update error, customer id={}, document={}",
                        customerDto.getId(), customerDto.getDocument(), exception))
                .flatMap(updatedAmount -> updatedAmount == 1
                        ? Mono.just(updatedAmount)
                        : Mono.error(new CustomerNotFoundException(updatedAmount)))
                .doOnSuccess(updated -> log.info("Customer id={} document updated", customerDto.getId()))
                .thenReturn(customerDto.getId())
                .flatMap(CustomerServiceImpl.this::getCustomerById);
    }

    @Override
    public Mono<DocumentDto> getDocumentByCustomerId(Long id) {
        return customerRepository.getDocumentById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                .flatMap(customersDocument -> Objects.nonNull(customersDocument.getDocument())
                        ? Mono.just(customersDocument.getDocument())
                        : Mono.error(new DocumentNotFoundException(customersDocument.getId())))
                .map(UUID::fromString)
                .flatMap(uuid -> findDocumentResource(id, uuid))
                .doOnError(CustomerNotFoundException.class, e -> log.warn(e.getMessage()))
                .doOnError(DocumentNotFoundException.class, e -> log.warn(e.getMessage()));
    }

    private Mono<DocumentDto> findDocumentResource(Long customerId, UUID uuid) {
        return documentClient.getDocument(uuid)
                .doOnError(DocumentResourceConsistencyException.class,
                        e -> log.error("Document resource was not found for customerId = {} by UUID={}", customerId, uuid, e))
                .flatMap(responseEntity -> {
                    if (responseEntity.getStatusCode().value() == HttpStatus.OK.value()) {
                        return Mono.just(DocumentDto.builder()
                                .title(responseEntity.getHeaders().getContentDisposition().getFilename())
                                .contentType(responseEntity.getHeaders().getContentType().toString())
                                .length(responseEntity.getHeaders().getContentLength())
                                .data(responseEntity.getBody())
                                .build());
                    } else {
                        return Mono.error(new RuntimeException("Unexpected response code: " + responseEntity.getStatusCode()
                                + " with body:" +  (responseEntity.getBody() == null ? "null" : new String(responseEntity.getBody()))));
                    }
                })
                .doOnError(RuntimeException.class,
                        e -> log.error("Unknown error happened due to resource request, customer id={} and document UUID={}", customerId, uuid, e));
    }
}
