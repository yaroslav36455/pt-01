package com.tyv.customerservice.controller;

import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.dto.DocumentDto;
import com.tyv.customerservice.dto.ExceptionDto;
import com.tyv.customerservice.exception.CustomerNotFoundException;
import com.tyv.customerservice.exception.DocumentNotFoundException;
import com.tyv.customerservice.service.CustomerService;
import com.tyv.customerservice.util.AddressUtil;
import com.tyv.customerservice.util.CustomerUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@WebFluxTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerService customerService;


    @Test
    @DisplayName("Возвращать customer и статус 200, если customer найден")
    void whenGetCustomerById_thenReturnCustomer() {
        Long customerId = 1L;
        CustomerDto customerDto = CustomerUtil.responseCustomerDto(customerId);

        Mockito.doReturn(Mono.just(customerDto))
                .when(customerService)
                .getCustomerById(customerId);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/customer/{id}").build(customerId))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDto.class)
                .isEqualTo(customerDto);

        Mockito.verify(customerService, Mockito.only()).getCustomerById(customerId);
    }

    @Test
    @DisplayName("Возвращать customer, address и статус 200, если customer найден")
    void whenGetCustomerByIdWithAddress_thenReturnCustomer() {
        long customerId = 1L;
        long addressId = 1L;
        CustomerDto customerDto = CustomerUtil.responseCustomerDto(customerId);
        AddressDto addressDto = AddressUtil.responseDto(addressId);
        customerDto.setAddress(addressDto);

        Mockito.doReturn(Mono.just(customerDto))
                .when(customerService)
                .getCustomerById(customerId);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/customer/{id}").build(customerId))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDto.class)
                .isEqualTo(customerDto);

        Mockito.verify(customerService, Mockito.only()).getCustomerById(customerId);
    }

    @Test
    @DisplayName("Возвращать статус 404 и сообщение об ошибке, если customer НЕ найден")
    void whenGetCustomerByIdAndCustomerWasNotFound_thenReturnError() {
        Long customerId = 1L;
        Mockito.doReturn(Mono.error(new CustomerNotFoundException(customerId)))
                .when(customerService)
                .getCustomerById(customerId);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/customer/{id}").build(customerId))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).isNotBlank();
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(customerService, Mockito.only()).getCustomerById(customerId);
    }

    @Test
    @DisplayName("Создать новый customer, вернуть статус 201 и новый customer")
    void whenCreateCustomer_thenReturnCustomer() {
        CustomerDto customerDtoRequest = CustomerUtil.requestCreateDto();
        CustomerDto customerDtoResponse = CustomerUtil.responseCustomerDto(1L);

        Mockito.doReturn(Mono.just(customerDtoResponse))
                .when(customerService)
                .createCustomer(Mockito.any(CustomerDto.class));

        webTestClient.post()
                .uri("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(customerDtoRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDto.class)
                .isEqualTo(customerDtoResponse);

        Mockito.verify(customerService, Mockito.only()).createCustomer(customerDtoRequest);
    }

    @Test
    @DisplayName("Передать новый customer с невалидными полями, вернуть статус 400 и сообщение об ошибке")
    void whenCreateCustomerWithInvalidFields_thenReturnError() {
        CustomerDto customerDtoRequest = CustomerUtil.requestCreateDto();
        String wrongName = "First Name";
        customerDtoRequest.setFirstName(wrongName);

        webTestClient.post()
                .uri("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(customerDtoRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).isNotBlank().contains(wrongName);
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(customerService, Mockito.never()).createCustomer(Mockito.any(CustomerDto.class));
    }

    @Test
    @DisplayName("Обновить существующий customer, вернуть статус 200 и обновлённый customer")
    void whenUpdateCustomer_thenReturnCustomer() {
        long customerId = 1L;
        CustomerDto customerDtoRequest = CustomerUtil.requestUpdateDto(customerId);
        CustomerDto customerDtoResponse = CustomerUtil.responseCustomerDto(customerId);

        Mockito.doReturn(Mono.just(customerDtoResponse))
                .when(customerService)
                .updateCustomer(Mockito.any(CustomerDto.class));

        webTestClient.put()
                .uri("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(customerDtoRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDto.class)
                .isEqualTo(customerDtoResponse);

        Mockito.verify(customerService, Mockito.only()).updateCustomer(customerDtoRequest);
    }

    @Test
    @DisplayName("Попытаться обновить customer, но customer не найден, вернуть статус 404 и сообщение об ошибке")
    void whenUpdateCustomerAndCustomerWasNotFound_thenReturnError() {
        Long customerId = 1L;
        CustomerDto customerDtoRequest = CustomerUtil.requestUpdateDto(customerId);
        Mockito.doReturn(Mono.error(new CustomerNotFoundException(customerId)))
                .when(customerService)
                .updateCustomer(customerDtoRequest);

        webTestClient.put()
                .uri("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(customerDtoRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).isNotBlank();
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(customerService, Mockito.only()).updateCustomer(customerDtoRequest);
    }

    @Test
    @DisplayName("Попытаться обновить customer, используя невалидные поля, вернуть статус 400 и сообщение об ошибке")
    void whenUpdateCustomerWithInvalidFields_thenReturnError() {
        CustomerDto customerDtoRequest = CustomerUtil.requestUpdateDto(null);
        String wrongCustomerName = "First Name";
        customerDtoRequest.setFirstName(wrongCustomerName);

        webTestClient.put()
                .uri("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(customerDtoRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).contains(List.of("null", wrongCustomerName));
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(customerService, Mockito.never()).updateCustomer(Mockito.any(CustomerDto.class));
    }

    @Test
    @DisplayName("Обновить document customer-a, вернуть статус 200 и обновленный customer")
    void whenUpdateDocumentCustomer_thenReturnCustomer() {
        long customerId = 1L;
        CustomerDto customerDtoRequest = CustomerUtil.requestUpdateDocumentDto(customerId);
        CustomerDto customerDtoResponse = CustomerUtil.responseCustomerDto(customerId);
        customerDtoResponse.setDocument(customerDtoRequest.getDocument());

        Mockito.doReturn(Mono.just(customerDtoResponse))
                .when(customerService)
                .updateCustomerDocument(customerDtoRequest);

        webTestClient.patch()
                .uri("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(customerDtoRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDto.class)
                .isEqualTo(customerDtoResponse);

        Mockito.verify(customerService, Mockito.only()).updateCustomerDocument(customerDtoRequest);
    }

    @Test
    @DisplayName("Попытаться обновить документ customer-а, но customer не найден, вернуть статус 404 и сообщение об ошибке")
    void whenUpdateDocumentCustomerAndCustomerWasNotFound_thenReturnError() {
        Long customerId = 1L;
        CustomerDto customerDtoRequest = CustomerUtil.requestUpdateDocumentDto(customerId);
        Mockito.doReturn(Mono.error(new CustomerNotFoundException(customerId)))
                .when(customerService)
                .updateCustomerDocument(customerDtoRequest);

        webTestClient.patch()
                .uri("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(customerDtoRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).isNotBlank();
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(customerService, Mockito.only()).updateCustomerDocument(customerDtoRequest);
    }

    @Test
    @DisplayName("Вернуть document и статус 200, если document был найден")
    void whenGetCustomerDocumentByCustomerId_thenReturnDocument() {
        long customerId = 1L;
        DocumentDto dto = CustomerUtil.responseDocumentDto(customerId);

        Mockito.doReturn(Mono.just(dto))
                .when(customerService)
                .getDocumentByCustomerId(customerId);

        webTestClient.get()
                .uri(builder -> builder.path("/api/customer/{id}/document").build(customerId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .expectHeader().contentLength(dto.getLength())
                .expectHeader().contentDisposition(ContentDisposition.builder("attachment").filename(dto.getTitle()).build())
                .expectBody(new ParameterizedTypeReference<byte[]>() {})
                .isEqualTo(dto.getData());

        Mockito.verify(customerService, Mockito.only()).getDocumentByCustomerId(customerId);
    }

    @Test
    @DisplayName("Если document или customer не были найдены, то вернуть статус 404 и сообщение об ошибке")
    void whenGetCustomerDocumentByCustomerIdAndCustomerOrDocumentWasNotFound_thenReturnError() {
        long customerId = 1L;

        Mockito.doReturn(Mono.error(new DocumentNotFoundException(customerId)))
                .when(customerService)
                .getDocumentByCustomerId(customerId);

        webTestClient.get()
                .uri(builder -> builder.path("/api/customer/{id}/document").build(customerId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).isNotBlank();
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(customerService, Mockito.only()).getDocumentByCustomerId(customerId);
    }
}