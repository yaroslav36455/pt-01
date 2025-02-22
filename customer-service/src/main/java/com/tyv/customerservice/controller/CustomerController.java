package com.tyv.customerservice.controller;


import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.dto.DocumentDto;
import com.tyv.customerservice.dto.group.CustomerDtoGroup.CreateRequest;
import com.tyv.customerservice.dto.group.CustomerDtoGroup.UpdateDocRequest;
import com.tyv.customerservice.dto.group.CustomerDtoGroup.UpdateRequest;
import com.tyv.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/customer")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{id}")
    public Mono<CustomerDto> getCustomer(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerDto> createCustomer(
            @RequestBody @Validated(CreateRequest.class) CustomerDto customerDto) {
        return customerService.createCustomer(customerDto);
    }

    @PutMapping
    public Mono<CustomerDto> updateCustomer(
            @RequestBody @Validated(UpdateRequest.class) CustomerDto customerDto) {
        return customerService.updateCustomer(customerDto);
    }

    @PatchMapping
    public Mono<CustomerDto> updateCustomersDocument(
            @RequestBody @Validated(UpdateDocRequest.class) CustomerDto customerDto) {
        return customerService.updateCustomerDocument(customerDto);
    }

    @GetMapping("/{id}/document")
    public Mono<ResponseEntity<byte[]>> getCustomerDocument(@PathVariable Long id) {
        return customerService.getDocumentByCustomerId(id)
                .map(documentDto -> ResponseEntity.ok()
                        .headers(createHttpHeaders(documentDto))
                        .body(documentDto.getData()));
    }

    private HttpHeaders createHttpHeaders(DocumentDto documentDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition
                .builder("attachment")
                .filename(documentDto.getTitle())
                .build());
        headers.setContentLength(documentDto.getLength());
        headers.set("File-Type", documentDto.getContentType());
        return headers;
    }
}
