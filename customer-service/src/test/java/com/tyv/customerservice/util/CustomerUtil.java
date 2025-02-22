package com.tyv.customerservice.util;

import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.dto.DocumentDto;
import org.assertj.core.api.Assertions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class CustomerUtil {

    public static CustomerDto requestCreateDto() {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setBirthDate(LocalDate.of(2000, 12, 15));
        customerDto.setPhone("+375(29)215-45-18");
        customerDto.setEmail("test.email@test.com");
        customerDto.setFirstName("Victor");
        customerDto.setLastName("Chehov");
        return customerDto;
    }

    public static CustomerDto requestUpdateDto(Long customerId) {
        CustomerDto customerDtoRequest = CustomerUtil.requestCreateDto();
        customerDtoRequest.setId(customerId);
        return customerDtoRequest;
    }

    public static CustomerDto requestUpdateDocumentDto(Long customerId) {
        CustomerDto customerDtoRequest = new CustomerDto();
        customerDtoRequest.setId(customerId);
        customerDtoRequest.setDocument(UUID.randomUUID());
        return customerDtoRequest;
    }

    static public CustomerDto responseCustomerDto(Long customerId) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(customerId);
        customerDto.setBirthDate(LocalDate.of(2000, 12, 15));
        customerDto.setPhone("+375(29)215-45-18");
        customerDto.setEmail("test.email@test.com");
        customerDto.setFirstName("Victor");
        customerDto.setLastName("Chehov");
        customerDto.setCreatedAt(LocalDateTime.of(2019, 3, 26,12,8,57));
        customerDto.setUpdatedAt(LocalDateTime.of(2020, 3, 29,8,6,24));
        customerDto.setDocument(UUID.fromString("02a1b62e-0bd2-4f2e-9117-956018769552"));
        return customerDto;
    }

    static public DocumentDto responseDocumentDto(Long customerId) {
        String data = "Just test data";
        DocumentDto documentDto = new DocumentDto();
        documentDto.setTitle("Some title");
        documentDto.setLength((long) data.length());
        documentDto.setData(data.getBytes());
        return documentDto;
    }

    public static void assertEqual(CustomerDto actual, CustomerDto expected) {
        if (Objects.nonNull(actual) && Objects.nonNull(expected)) {
            Assertions.assertThat(actual.getId()).isEqualTo(expected.getId());
            Assertions.assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
            Assertions.assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
            Assertions.assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
            Assertions.assertThat(actual.getPhone()).isEqualTo(expected.getPhone());
            Assertions.assertThat(actual.getBirthDate()).isEqualTo(expected.getBirthDate());
            Assertions.assertThat(actual.getDocument()).isEqualTo(expected.getDocument());
            Assertions.assertThat(actual.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            Assertions.assertThat(actual.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            AddressUtil.assertEqual(actual.getAddress(), expected.getAddress());
        } else {
            Assertions.assertThat((actual == null) == (expected == null)).isTrue();
        }
    }
}
