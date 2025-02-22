package com.tyv.customerservice.repository;

import com.tyv.customerservice.entity.Customer;
import com.tyv.customerservice.entity.CustomersDocument;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Transactional
    @Modifying
    @Query(value = "update Customer e set e.document=:uuid where e.id=:id")
    void updateCustomerDocument(@Param("id") Long id, @Param("uuid") UUID uuid);

    Optional<CustomersDocument> getDocumentById(Long id);
}
