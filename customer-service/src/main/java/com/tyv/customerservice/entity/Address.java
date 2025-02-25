package com.tyv.customerservice.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "address")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {
    @Id
    Long id;

    @CreatedDate
    @Column("created_at")
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    LocalDateTime updatedAt;

    String settlement;
    String street;
    String building;

    @Column("customer_id")
    Long customerId;
}
