package com.tyv.customerservice.service;

import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.entity.Address;
import com.tyv.customerservice.exception.AddressNotFoundException;
import com.tyv.customerservice.mapper.AddressMapper;
import com.tyv.customerservice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;


    @Override
    public Mono<AddressDto> getAddressById(Long id) {
        return findAddressById(id)
                .map(addressMapper::toDto);
    }

    @Override
    public Mono<AddressDto> createAddress(AddressDto addressDto) {
        return Mono.just(addressDto)
                .map(addressMapper::toEntity)
                .flatMap(addressRepository::save)
                .doOnSuccess(address -> log.info("New address created with id={} for customer with id={}",
                        address.getId(), address.getCustomerId()))
                .map(addressMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<AddressDto> updateAddress(AddressDto addressDto) {
        return findAddressById(addressDto.getId())
                .map(address -> {
                    addressMapper.toEntityUpdate(address, addressDto);
                    return address;
                })
                .onErrorMap(NoSuchElementException.class, e -> new AddressNotFoundException(addressDto.getId()))
                .flatMap(addressRepository::save)
                .doOnSuccess(address -> log.info("Address updated with id={} for customer with id={}",
                        address.getId(), address.getCustomerId()))
                .map(addressMapper::toDto);
    }

    private Mono<Address> findAddressById(Long id) {
        return addressRepository.findById(id)
                .switchIfEmpty(Mono.error(new AddressNotFoundException(id)))
                .doOnError(AddressNotFoundException.class, exception -> log.warn(exception.toString()));

    }
}
