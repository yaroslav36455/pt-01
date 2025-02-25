package com.tyv.customerservice.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "customer")
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer {
    @Id
    Long id;

    @CreatedDate
    @Column("created_at")
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    LocalDateTime updatedAt;

    @Column("first_name")
    String firstName;

    @Column("last_name")
    String lastName;

    String email;
    String phone;

    @Column("birth_date")
    LocalDate birthDate;

    UUID document;

    @Transient
    Address address;
}
