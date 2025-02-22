package com.tyv.customerservice.service;

import com.tyv.customerservice.client.DocumentClient;
import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.dto.DocumentDto;
import com.tyv.customerservice.exception.CustomerNotFoundException;
import com.tyv.customerservice.exception.DocumentNotFoundException;
import com.tyv.customerservice.exception.DocumentResourceConsistencyException;
import com.tyv.customerservice.mapper.CustomerMapper;
import com.tyv.customerservice.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    DocumentClient documentClient;

    @Override
    public Mono<CustomerDto> getCustomerById(Long id) {
        return Mono.just(id)
                .publishOn(Schedulers.boundedElastic())
                .map(customerRepository::findById)
                .filter(Optional::isPresent)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                .map(Optional::get)
                .map(customerMapper::toDto)
                .doOnError(CustomerNotFoundException.class, exception -> log.warn(exception.toString()));
    }

    @Override
    public Mono<CustomerDto> createCustomer(CustomerDto customerDto) {
        return Mono.just(customerDto)
                .publishOn(Schedulers.boundedElastic())
                .map(customerMapper::toEntity)
                .map(customerRepository::save)
                .doOnSuccess(customer -> log.info("Customer {} created", customer.getId()))
                .map(customerMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<CustomerDto> updateCustomer(CustomerDto customerDto) {
        return Mono.just(customerDto)
                .flatMap(dto -> Mono.fromCallable(() -> customerRepository.findById(dto.getId()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerDto.getId())))
                        .map(Optional::get)
                        .map(customer -> {
                            customerMapper.toEntityUpdate(customer, dto);
                            return customer;
                        }))
                .map(customerRepository::save)
                .doOnSuccess(customer -> log.info("Customer id={} updated", customer.getId()))
                .onErrorMap(NoSuchElementException.class, e -> new CustomerNotFoundException(customerDto.getId()))
                .doOnError(CustomerNotFoundException.class, customer -> log.warn(customer.toString()))
                .map(customerMapper::toDto);
    }

    @Override
    public Mono<CustomerDto> updateCustomerDocument(CustomerDto customerDto) {
        return Mono.just(customerDto)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(dto -> customerRepository.updateCustomerDocument(dto.getId(), dto.getDocument()))
                .doOnSuccess(customer -> log.info("Customer id={} document updated", customer.getId()))
                .map(CustomerDto::getId)
                .flatMap(CustomerServiceImpl.this::getCustomerById);
    }

    @Override
    public Mono<DocumentDto> getDocumentByCustomerId(Long id) {
        return Mono.fromCallable(() -> customerRepository.getDocumentById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Optional::isPresent)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                .map(Optional::get)
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
