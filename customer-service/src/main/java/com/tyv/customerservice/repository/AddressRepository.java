package com.tyv.customerservice.repository;

import com.tyv.customerservice.entity.Address;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AddressRepository extends ReactiveCrudRepository<Address, Long> {
    Mono<Address> getAddressByCustomerId(Long customerId);
}
