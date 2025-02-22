package com.tyv.customerservice.controller;

import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.dto.ExceptionDto;
import com.tyv.customerservice.exception.AddressNotFoundException;
import com.tyv.customerservice.service.AddressService;
import com.tyv.customerservice.util.AddressUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@WebFluxTest(AddressController.class)
class AddressControllerTest {
    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private AddressService addressService;

    @Test
    @DisplayName("Возвращать address и статус 200, если address найден")
    void whenGetAddressById_thenReturnAddress() {
        Long addressId = 1L;
        AddressDto addressDto = AddressUtil.responseDto(addressId);

        Mockito.doReturn(Mono.just(addressDto))
                .when(addressService)
                .getAddressById(addressId);

        webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/address/{id}").build(addressId))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AddressDto.class)
                .isEqualTo(addressDto);

        Mockito.verify(addressService, Mockito.only()).getAddressById(addressId);
    }

    @Test
    @DisplayName("Возвращать статус 404 и сообщение об ошибке, если address НЕ найден")
    void whenGetAddressByIdAndAddressWasNotFound_thenReturnError() {
        Long addressId = 1L;
        Mockito.doReturn(Mono.error(new AddressNotFoundException(addressId)))
                .when(addressService)
                .getAddressById(addressId);

        webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/address/{id}").build(addressId))
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

        Mockito.verify(addressService, Mockito.only()).getAddressById(addressId);
    }

    @Test
    @DisplayName("Создать новый address, вернуть статус 201 и новый address")
    void whenCreateAddress_thenReturnAddress() {
        Long addressId = 1L;
        AddressDto requestDto = AddressUtil.requestCreateDto();
        AddressDto responseDto = AddressUtil.responseDto(addressId);

        Mockito.doReturn(Mono.just(responseDto))
                .when(addressService)
                .createAddress(requestDto);

        webClient.post()
                .uri("/api/address")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AddressDto.class)
                .isEqualTo(responseDto);

        Mockito.verify(addressService, Mockito.only()).createAddress(requestDto);
    }

    @Test
    @DisplayName("Передать новый address с невалидными полями, вернуть статус 400 и сообщение об ошибке")
    void whenCreateAddressWithInvalidFields_thenReturnError() {
        Long addressId = 1L;
        AddressDto requestDto = AddressUtil.requestCreateDto();
        requestDto.setStreet(null);

        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/address").build(addressId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).contains(List.of("null"));
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(addressService, Mockito.never()).createAddress(Mockito.any(AddressDto.class));
    }

    @Test
    @DisplayName("Обновить существующий address, вернуть статус 200 и обновлённый address")
    void whenUpdateAddress_thenReturnAddress() {
        Long addressId = 1L;
        AddressDto requestDto = AddressUtil.requestUpdateDto(addressId);
        AddressDto responseDto = AddressUtil.responseDto(addressId);

        Mockito.doReturn(Mono.just(responseDto))
                .when(addressService)
                .updateAddress(requestDto);

        webClient.put()
                .uri("/api/address")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AddressDto.class)
                .isEqualTo(responseDto);

        Mockito.verify(addressService, Mockito.only()).updateAddress(requestDto);
    }

    @Test
    @DisplayName("Попытаться обновить address, но address не найден, вернуть статус 404 и сообщение об ошибке")
    void whenUpdateAddressAndAddressWasNotFound_thenReturnError() {
        Long addressId = 1L;
        AddressDto requestDto = AddressUtil.requestUpdateDto(addressId);

        Mockito.doReturn(Mono.error(new AddressNotFoundException(addressId)))
                .when(addressService)
                .updateAddress(requestDto);

        webClient.put()
                .uri("/api/address")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).isNotBlank();
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(addressService, Mockito.only()).updateAddress(requestDto);
    }

    @Test
    @DisplayName("Попытаться обновить address с невалидными полями, вернуть статус 400 и сообщение об ошибке")
    void whenUpdateAddressWithInvalidFields_thenReturnError() {
        AddressDto requestDto = AddressUtil.requestUpdateDto(null);
        requestDto.setId(null);
        requestDto.setStreet(null);

        webClient.put()
                .uri("/api/address")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ExceptionDto.class)
                .value(exceptionDto -> {
                    Assertions.assertThat(exceptionDto).isNotNull();
                    Assertions.assertThat(exceptionDto.getErrorMessage()).contains(List.of("null"));
                    Assertions.assertThat(exceptionDto.getTimestamp()).isBefore(LocalDateTime.now());
                });

        Mockito.verify(addressService, Mockito.never()).updateAddress(requestDto);
    }
}