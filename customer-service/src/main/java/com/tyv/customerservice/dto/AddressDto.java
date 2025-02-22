package com.tyv.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tyv.customerservice.dto.group.AddressDtoGroup.CreateRequest;
import com.tyv.customerservice.dto.group.AddressDtoGroup.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressDto {
    @NotNull(groups = UpdateRequest.class)
    Long id;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;

    @NotBlank(groups = {CreateRequest.class, UpdateRequest.class})
    String settlement;

    @NotBlank(groups = {CreateRequest.class, UpdateRequest.class})
    String street;

    @NotBlank(groups = {CreateRequest.class, UpdateRequest.class})
    String building;

    @JsonProperty("customer_id")
    @NotNull(groups = CreateRequest.class)
    Long customerId;
}
