package com.tyv.customerservice.controller;

import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.dto.group.AddressDtoGroup.CreateRequest;
import com.tyv.customerservice.dto.group.AddressDtoGroup.UpdateRequest;
import com.tyv.customerservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/address")
@RequiredArgsConstructor
@Validated
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/{id}")
    public Mono<AddressDto> getAddress(@PathVariable Long id) {
        return addressService.getAddressById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AddressDto> createAddress(@RequestBody @Validated(CreateRequest.class) AddressDto addressDto) {
        return addressService.createAddress(addressDto);
    }

    @PutMapping
    public Mono<AddressDto> updateAddress(@RequestBody @Validated(UpdateRequest.class) AddressDto addressDto) {
        return addressService.updateAddress(addressDto);
    }
}
