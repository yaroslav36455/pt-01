package com.tyv.customerservice.service;

import com.tyv.customerservice.TestcontainersConfiguration;
import com.tyv.customerservice.client.DocumentClient;
import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.exception.AddressNotFoundException;
import com.tyv.customerservice.repository.AddressRepository;
import com.tyv.customerservice.util.AddressUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static com.tyv.customerservice.util.AddressUtil.assertEqual;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AddressServiceImplTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @MockitoBean
    private DocumentClient documentClient;


    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Возвращать address, если address найден")
    void whenGetAddressById_thenReturnAddress() {
        long addressId = 1L;
        AddressDto responseDto = AddressUtil.responseDto(addressId);

        StepVerifier.create(addressService.getAddressById(addressId))
                .assertNext(addressDto -> assertEqual(addressDto, responseDto))
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql"})
    @DisplayName("Возвращать исключение, если address НЕ найден")
    void whenGetAddressByIdAndAddressWasNotFound_thenReturnException() {
        long addressId = 1L;

        StepVerifier.create(addressService.getAddressById(addressId))
                .expectError(AddressNotFoundException.class)
                .verify();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql"})
    @DisplayName("Создать новый address, вернуть новый address")
    void whenCreateAddress_thenReturnAddress() {
        long addressId = 1L;
        AddressDto requestDto = AddressUtil.requestCreateDto();
        AddressDto responseDto = AddressUtil.responseDto(addressId);

        StepVerifier.create(addressService.createAddress(requestDto))
                .assertNext(addressDto -> assertEqual(addressDto, responseDto))
                .verifyComplete();

        StepVerifier.create(addressRepository.findAll())
                .assertNext(address -> {
                    Assertions.assertThat(address.getId()).isEqualTo(responseDto.getId());
                    Assertions.assertThat(address.getSettlement()).isEqualTo(responseDto.getSettlement());
                    Assertions.assertThat(address.getStreet()).isEqualTo(responseDto.getStreet());
                    Assertions.assertThat(address.getBuilding()).isEqualTo(responseDto.getBuilding());
                    Assertions.assertThat(address.getCustomerId()).isEqualTo(responseDto.getCustomerId());
                    Assertions.assertThat(address.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
                    Assertions.assertThat(address.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
                })
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Попытаться создать новый address для customer у которого уже есть address, вернуть ошибку")
    void whenCreateAddressForCustomerThatAlreadyHaveAddress_thenReturnError() {
        AddressDto requestDto = AddressUtil.requestCreateDto();

        StepVerifier.create(addressService.createAddress(requestDto))
                .expectError(DataIntegrityViolationException.class)
                .verify();

        StepVerifier.create(addressRepository.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql"})
    @DisplayName("Попытаться создать новый address для customer которого не сущетсвует, вернуть ошибку")
    void whenCreateAddressForCustomerThatIsNotExists_thenReturnError() {
        AddressDto requestDto = AddressUtil.requestCreateDto();

        StepVerifier.create(addressService.createAddress(requestDto))
                .expectError(DataIntegrityViolationException.class)
                .verify();

        StepVerifier.create(addressRepository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Обновить существующий address, вернуть обновлённый address")
    void whenUpdateAddress_thenReturnAddress() {
        long addressId = 1L;
        String newStreetName = "Tomas Street";
        AddressDto requestDto = AddressUtil.requestUpdateDto(addressId);
        AddressDto responseDto = AddressUtil.responseDto(addressId);
        requestDto.setStreet(newStreetName);
        responseDto.setStreet(newStreetName);

        StepVerifier.create(addressService.updateAddress(requestDto))
                .assertNext(addressDto -> assertEqual(addressDto, responseDto))
                .verifyComplete();

        StepVerifier.create(addressRepository.findAll())
                .assertNext(address -> {
                    Assertions.assertThat(address.getId()).isEqualTo(responseDto.getId());
                    Assertions.assertThat(address.getSettlement()).isEqualTo(responseDto.getSettlement());
                    Assertions.assertThat(address.getStreet()).isEqualTo(responseDto.getStreet());
                    Assertions.assertThat(address.getBuilding()).isEqualTo(responseDto.getBuilding());
                    Assertions.assertThat(address.getCustomerId()).isEqualTo(responseDto.getCustomerId());
                    Assertions.assertThat(address.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
                    Assertions.assertThat(address.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
                })
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql"})
    @DisplayName("Попытаться обновить address, но address не найден, вернуть искюсение")
    void whenUpdateAddressAndAddressWasNotFound_thenReturnException() {
        long addressId = 1L;
        AddressDto requestDto = AddressUtil.requestUpdateDto(addressId);

        StepVerifier.create(addressService.updateAddress(requestDto))
                .expectError(AddressNotFoundException.class)
                .verify();

        StepVerifier.create(addressRepository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }
}