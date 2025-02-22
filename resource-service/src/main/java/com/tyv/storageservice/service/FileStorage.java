package com.tyv.storageservice.service;

import com.tyv.storageservice.entity.StorageResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileStorage {

    Mono<StorageResource> getData(StorageResource resource);
    Mono<Void> save(StorageResource storageResource, FilePart filePart);

    Mono<Boolean> deleteFile(StorageResource storageResource);
}
