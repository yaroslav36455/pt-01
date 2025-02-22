package com.tyv.customerservice.service;

import com.tyv.customerservice.dto.AddressDto;
import reactor.core.publisher.Mono;

public interface AddressService {
    Mono<AddressDto> getAddressById(Long id);
    Mono<AddressDto> createAddress(AddressDto addressDto);
    Mono<AddressDto> updateAddress(AddressDto addressDto);
}
