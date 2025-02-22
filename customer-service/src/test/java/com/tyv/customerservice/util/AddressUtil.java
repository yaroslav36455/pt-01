package com.tyv.customerservice.util;

import com.tyv.customerservice.dto.AddressDto;
import org.assertj.core.api.Assertions;

import java.time.LocalDateTime;
import java.util.Objects;

public class AddressUtil {
    public static AddressDto requestCreateDto() {
        AddressDto addressDto = new AddressDto();
        addressDto.setCustomerId(1L);
        addressDto.setSettlement("Sydney");
        addressDto.setStreet("Adams Street");
        addressDto.setBuilding("142b");
        return addressDto;
    }

    public static AddressDto responseDto(Long addressId) {
        AddressDto addressDto = new AddressDto();
        addressDto.setId(addressId);
        addressDto.setCustomerId(1L);
        addressDto.setSettlement("Sydney");
        addressDto.setStreet("Adams Street");
        addressDto.setBuilding("142b");
        addressDto.setCreatedAt(LocalDateTime.of(2019, 3, 26,12,8,57));
        addressDto.setUpdatedAt(LocalDateTime.of(2020, 3, 29,8,6,24));
        return addressDto;
    }

    public static AddressDto requestUpdateDto(Long addressId) {
        AddressDto addressDto = requestCreateDto();

        addressDto.setId(addressId);
        return addressDto;
    }

    public static void assertEqual(AddressDto actual, AddressDto expected) {
        if (Objects.nonNull(actual) && Objects.nonNull(expected)) {
            Assertions.assertThat(actual.getId()).isEqualTo(expected.getId());
            Assertions.assertThat(actual.getSettlement()).isEqualTo(expected.getSettlement());
            Assertions.assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            Assertions.assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            Assertions.assertThat(actual.getCustomerId()).isEqualTo(expected.getCustomerId());
            Assertions.assertThat(actual.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            Assertions.assertThat(actual.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        } else {
            Assertions.assertThat((actual == null) == (expected == null)).isTrue();
        }

    }
}
