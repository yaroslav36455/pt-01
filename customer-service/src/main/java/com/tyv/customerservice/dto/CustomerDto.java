package com.tyv.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tyv.customerservice.dto.group.CustomerDtoGroup.CreateRequest;
import com.tyv.customerservice.dto.group.CustomerDtoGroup.UpdateDocRequest;
import com.tyv.customerservice.dto.group.CustomerDtoGroup.UpdateRequest;
import com.tyv.customerservice.util.Formatter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerDto {
    @NotNull(groups = UpdateRequest.class)
    Long id;

    @JsonProperty("created_at")
    @JsonFormat(pattern = Formatter.DATE_TIME_PATTERN_PUBLIC)
    LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = Formatter.DATE_TIME_PATTERN_PUBLIC)
    LocalDateTime updatedAt;

    @JsonProperty("first_name")
    @Pattern(regexp = "^\\p{javaUpperCase}\\p{javaLowerCase}{2,}$", groups = {CreateRequest.class, UpdateRequest.class})
    String firstName;

    @JsonProperty("last_name")
    @Pattern(regexp = "^\\p{javaUpperCase}\\p{javaLowerCase}{2,}$", groups = {CreateRequest.class, UpdateRequest.class})
    String lastName;

    @Email(regexp = "^[A-Za-z0-9._+%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$", groups = {CreateRequest.class, UpdateRequest.class})
    String email;

    @Pattern(regexp = "^\\+3\\d{2}\\(\\d{2}\\)\\d{3}-\\d{2}-\\d{2}$", groups = {CreateRequest.class, UpdateRequest.class})
    String phone;

    @JsonProperty("birth_date")
    @NotNull(groups = {CreateRequest.class, UpdateRequest.class})
    LocalDate birthDate;

    @NotNull(groups = UpdateDocRequest.class)
    UUID document;

    AddressDto address;
}
