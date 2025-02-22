package com.tyv.storageservice.repository;

import com.tyv.storageservice.entity.StorageResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ResourceRepository extends CrudRepository<StorageResource, String> {
    Optional<StorageResource> findByUuid(UUID uuid);
}
