package com.tyv.customerservice.service;

import com.tyv.customerservice.TestcontainersConfiguration;
import com.tyv.customerservice.client.DocumentClient;
import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.entity.Customer;
import com.tyv.customerservice.exception.CustomerNotFoundException;
import com.tyv.customerservice.repository.CustomerRepository;
import com.tyv.customerservice.util.AddressUtil;
import com.tyv.customerservice.util.CustomerUtil;
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

import java.util.List;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CustomerServiceImplTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @MockitoBean
    private DocumentClient documentClient;

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Возвращать customer, если customer найден")
    void whenGetCustomerById_thenReturnCustomer() {
        long customerId = 1L;
        long addressId = 1L;
        CustomerDto customerDto = CustomerUtil.responseCustomerDto(customerId);
        AddressDto addressDto = AddressUtil.responseDto(addressId);
        customerDto.setAddress(addressDto);

        StepVerifier.create(customerService.getCustomerById(customerId))
                .assertNext(dto -> CustomerUtil.assertEqual(dto, customerDto))
                .verifyComplete();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql"})
    @DisplayName("Возвращать сообщение об ошибке, если customer НЕ найден")
    void whenGetCustomerByIdAndCustomerWasNotFound_thenReturnError() {
        StepVerifier.create(customerService.getCustomerById(1L))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql"})
    @DisplayName("Создать новый customer, вернуть новый customer")
    void whenCreateCustomer_thenReturnCustomer() {
        long customerId = 1L;
        CustomerDto customerRequestDto = CustomerUtil.requestCreateDto();
        CustomerDto customerResponseDto = CustomerUtil.responseCustomerDto(customerId);
        customerResponseDto.setDocument(null);

        StepVerifier.create(customerService.createCustomer(customerRequestDto))
                .assertNext(actual -> CustomerUtil.assertEqual(actual, customerResponseDto))
                .verifyComplete();

        List<Customer> customers = customerRepository.findAll();
        Assertions.assertThat(customers).hasSize(1);
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Попытаться создать новый customer с дублирующими полями, вернуть ошибку")
    void whenCreateCustomerButCustomerWithSuchEmailAlreadyExists_thenReturnException() {
        CustomerDto customerRequestDto = CustomerUtil.requestCreateDto();

        StepVerifier.create(customerService.createCustomer(customerRequestDto))
                .expectError(DataIntegrityViolationException.class)
                .verify();

        List<Customer> customers = customerRepository.findAll();
        Assertions.assertThat(customers).hasSize(1);
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Обновить существующий customer, вернуть обновлённый customer")
    void whenUpdateCustomer_thenReturnCustomer() {
        long customerId = 1L;
        long addressId = 1L;
        String newEmail = "some.new@email.com";
        CustomerDto customerRequestDto = CustomerUtil.requestUpdateDto(customerId);
        CustomerDto customerResponseDto = CustomerUtil.responseCustomerDto(customerId);
        customerRequestDto.setEmail(newEmail);
        customerResponseDto.setEmail(newEmail);
        customerRequestDto.setDocument(customerResponseDto.getDocument());
        customerResponseDto.setAddress(AddressUtil.responseDto(addressId));

        StepVerifier.create(customerService.updateCustomer(customerRequestDto))
                .assertNext(actual -> CustomerUtil.assertEqual(actual, customerResponseDto))
                .verifyComplete();

        List<Customer> customers = customerRepository.findAll();
        Assertions.assertThat(customers).hasSize(1);
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql"})
    @DisplayName("Попытаться обновить customer, но customer не найден, вернуть исключение")
    void whenUpdateCustomerAndCustomerWasNotFound_thenReturnException() {
        long customerId = 1L;
        String newEmail = "some.new@email.com";
        CustomerDto customerRequestDto = CustomerUtil.requestUpdateDto(customerId);
        customerRequestDto.setEmail(newEmail);

        StepVerifier.create(customerService.updateCustomer(customerRequestDto))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Обновить document customer-a, вернуть обновленный customer")
    void whenUpdateDocumentCustomer_thenReturnCustomer() {
        long customerId = 1L;
        long addressId = 1L;
        CustomerDto customerRequestDto = CustomerUtil.requestUpdateDocumentDto(customerId);
        CustomerDto customerResponseDto = CustomerUtil.responseCustomerDto(customerId);
        customerResponseDto.setDocument(customerRequestDto.getDocument());
        customerResponseDto.setAddress(AddressUtil.responseDto(addressId));

        StepVerifier.create(customerService.updateCustomerDocument(customerRequestDto))
                .assertNext(actual -> CustomerUtil.assertEqual(actual, customerResponseDto))
                .verifyComplete();

        List<Customer> customers = customerRepository.findAll();
        Assertions.assertThat(customers).hasSize(1);
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Попытаться обновить документ customer-а, но customer не найден, вернуть исключение")
    void whenUpdateDocumentCustomerAndCustomerWasNotFound_thenReturnException() {
        long customerId = 2L;
        CustomerDto customerRequestDto = CustomerUtil.requestUpdateDocumentDto(customerId);

        StepVerifier.create(customerService.updateCustomerDocument(customerRequestDto))
                .expectError(CustomerNotFoundException.class)
                .verify();

        List<Customer> customers = customerRepository.findAll();
        Assertions.assertThat(customers).hasSize(1);
    }
}