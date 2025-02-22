package com.tyv.customerservice.service;

import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.entity.Address;
import com.tyv.customerservice.exception.AddressNotFoundException;
import com.tyv.customerservice.mapper.AddressMapper;
import com.tyv.customerservice.repository.AddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;


    @Override
    public Mono<AddressDto> getAddressById(Long id) {
        return Mono.fromSupplier(() -> addressRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Optional::isPresent)
                .switchIfEmpty(Mono.error(new AddressNotFoundException(id)))
                .map(Optional::get)
                .map(addressMapper::toDto)
                .doOnError(AddressNotFoundException.class, exception -> log.warn(exception.toString()));
    }

    @Override
    public Mono<AddressDto> createAddress(AddressDto addressDto) {
        return Mono.just(addressDto)
                .publishOn(Schedulers.boundedElastic())
                .map(addressMapper::toEntity)
                .map(addressRepository::save)
                .doOnSuccess(address -> log.info("New address created with id={} for customer with id={}",
                        address.getId(), address.getCustomerId()))
                .map(addressMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<AddressDto> updateAddress(AddressDto addressDto) {
        return Mono.just(addressDto)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(dto -> findAddressById(dto.getId())
                        .map(address -> {
                            addressMapper.toEntityUpdate(address, addressDto);
                            return address;
                        }))
                .onErrorMap(NoSuchElementException.class, e -> new AddressNotFoundException(addressDto.getId()))
                .map(addressRepository::save)
                .doOnSuccess(address -> log.info("Address updated with id={} for customer with id={}",
                        address.getId(), address.getCustomerId()))
                .map(addressMapper::toDto);
    }

    private Mono<Address> findAddressById(Long id) {
        return Mono.fromCallable(() -> addressRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new AddressNotFoundException(id)))
                .map(Optional::get)
                .doOnError(AddressNotFoundException.class, exception -> log.warn(exception.toString()));

    }
}
