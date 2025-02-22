package com.tyv.customerservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer")
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @CreatedDate
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    String email;
    String phone;

    @Column(name = "birth_date")
    LocalDate birthDate;

    UUID document;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    Address address;
}
