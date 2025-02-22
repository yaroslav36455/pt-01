package com.tyv.storageservice.service;

import com.tyv.storageservice.entity.StorageResource;
import com.tyv.storageservice.model.Metadata;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface Storage {
    Mono<StorageResource> getFileDataByUUID(Mono<String> uuid);
    Mono<UUID> createResourceAndGetUUID(Mono<Metadata> metadata, Mono<FilePart> file);
    Mono<List<UUID>> createResourceListAndGetUUIDs(Mono<Metadata> metadata, Flux<FilePart> files);
    Mono<Void> deleteResourceByUUID(Mono<String> uuid);
}
