package com.tyv.storageservice.service;

import com.tyv.storageservice.model.Metadata;
import com.tyv.storageservice.entity.StorageResource;
import com.tyv.storageservice.exception.ResourceNotFoundException;
import com.tyv.storageservice.exception.ResourceReadingException;
import com.tyv.storageservice.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class StorageImpl implements Storage {

    private final FileStorage fileStorage;
    private final ResourceRepository resourceRepository;

    @Override
    public Mono<StorageResource> getFileDataByUUID(Mono<String> uuid) {
        return uuid.flatMap(uuidStr ->
                        Mono.fromCallable(() -> resourceRepository.findByUuid(UUID.fromString(uuidStr)))
                                .subscribeOn(Schedulers.boundedElastic())
                                .filter(Optional::isPresent)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Resource by UUID [" + uuidStr + "] not found")))
                                .map(Optional::get)
                                .flatMap(fileStorage::getData)
                                .onErrorMap(throwable -> ! (throwable instanceof ResourceNotFoundException),
                                        e -> new ResourceReadingException("Resource reading error, UUID=" + uuidStr, e))
                                )
                .doOnError(ResourceNotFoundException.class, error -> log.warn(error.getMessage()))
                .doOnError(throwable -> ! (throwable instanceof ResourceNotFoundException),
                        error -> log.error(error.getMessage(), error));
    }

    @Override
    public Mono<UUID> createResourceAndGetUUID(Mono<Metadata> metadata, Mono<FilePart> file) {
        return metadata.flatMap(metadataRequestDto ->
                        file.flatMap(filePart -> createResource(metadataRequestDto, filePart))
                )
                .doOnSuccess(uuid -> log.info("Resource created: UUID={}", uuid))
                .doOnError(throwable -> log.error("Resource creation error", throwable));
    }

    @Override
    public Mono<List<UUID>> createResourceListAndGetUUIDs(Mono<Metadata> metadata, Flux<FilePart> files) {
        return metadata.flatMapMany(metadataRequestDto ->
                        files.flatMap(filePart -> createResource(metadataRequestDto, filePart))
                ).collectList()
                .doOnSuccess(uuid -> log.info("Resource created: UUID={}", uuid))
                .doOnError(throwable -> log.error("Resource list creation error", throwable));
    }

    private Mono<UUID> createResource(Metadata metadata, FilePart filePart) {
        StorageResource storageResource = createStorageResource(metadata, filePart);
        Mono<Void> saveMetadata = Mono.fromCallable(() -> resourceRepository.save(storageResource))
                .subscribeOn(Schedulers.boundedElastic())
                .then();

        Mono<Void> saveFile = fileStorage.save(storageResource, filePart);

        return saveMetadata
                .then(saveFile)
                .onErrorResume(ex ->
                        Mono.fromRunnable(() -> resourceRepository.delete(storageResource))
                                .subscribeOn(Schedulers.boundedElastic())
                                .then(Mono.error(ex)))
                .thenReturn(storageResource.getUuid());
    }

    private StorageResource createStorageResource(Metadata metadata, FilePart filePart) {
        UUID uuid = UUID.randomUUID();
        Path path = createPath(uuid, filePart.filename());

        return StorageResource.builder()
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .category(metadata.category())
                .bucket(metadata.bucket())
                .uuid(uuid)
                .title(filePart.filename())
                .path(path.toString())
                .contentType(Objects.requireNonNull(filePart.headers().getContentType()).toString())
                .build();
    }

    private Path createPath(UUID uuid, String filename) {
        return Path.of(LocalDate.now().toString())
                .resolve(uuid.toString() + '-' + filename);
    }

    @Override
    public Mono<Void> deleteResourceByUUID(Mono<String> uuidMono) {
        return uuidMono.flatMap(uuid ->
                        Mono.fromCallable(() -> resourceRepository.findByUuid(UUID.fromString(uuid)))
                                .subscribeOn(Schedulers.boundedElastic())
                                .filter(Optional::isPresent)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Resource by uuid [" + uuid + "] not found")))
                                .map(Optional::get)
                                .flatMap(resource ->
                                        fileStorage.deleteFile(resource)
                                                .filter(Boolean::booleanValue)
                                                .switchIfEmpty(Mono.error(new RuntimeException("File deletion failed: UUID=" + uuid)))
                                                .flatMap(result -> Mono.fromRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        resourceRepository.delete(resource);
                                                    }
                                                })))
                                .then()
                                .doOnSuccess(v -> log.info("Resource deleted: UUID={}", uuid))
                                .doOnError(throwable -> log.warn("Resource deletion failed: UUID={}", uuid))
                );
    }
}
