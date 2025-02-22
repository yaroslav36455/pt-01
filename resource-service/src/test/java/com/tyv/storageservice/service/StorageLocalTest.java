package com.tyv.storageservice.service;

import com.tyv.storageservice.TestcontainersPostgresConfiguration;
import com.tyv.storageservice.model.Metadata;
import com.tyv.storageservice.entity.StorageResource;
import com.tyv.storageservice.enums.Bucket;
import com.tyv.storageservice.enums.Category;
import com.tyv.storageservice.exception.ResourceNotFoundException;
import com.tyv.storageservice.exception.ResourceReadingException;
import com.tyv.storageservice.repository.ResourceRepository;
import com.tyv.storageservice.util.FileConstants;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

import static com.tyv.storageservice.util.LocalStorageResourceUtil.*;

@Import(TestcontainersPostgresConfiguration.class)
@SpringBootTest(properties = "spring.profiles.active=localStorage")
class StorageLocalTest {
    @Autowired
    private Storage storage;

    @Autowired
    private ResourceRepository resourceRepository;

    @Value("${storage.path}")
    private Path storagePath;


    @AfterEach
    void tearDown() throws IOException {
        deleteDirectory(storagePath);
    }


    @Test
    @Sql(scripts = {
            "/scripts/cleanup_resource.sql",
            "/scripts/insert_resource.sql"})
    @DisplayName("Ресурс с указанным UUID существует, вернуть данные")
    void whenResourceExists_thenGetFileData() throws IOException {
        prepareTestDirectory(storagePath);

        Mono<StorageResource> fileDataByUUID = storage.getFileDataByUUID(Mono.just(FileConstants.FROG.getUuid()));

        StepVerifier.create(fileDataByUUID)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    @Sql(scripts = "/scripts/cleanup_resource.sql")
    @DisplayName("Ресурс с указанным UUID не существует, сообщение об отсутствии ресурса")
    void whenResourceNotExist_thenGetError() {
        Mono<StorageResource> filePathForUUID = storage.getFileDataByUUID(Mono.just(FileConstants.FROG.getUuid()));

        StepVerifier.create(filePathForUUID)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_resource.sql",
            "/scripts/insert_resource.sql"})
    @DisplayName("Ресурс с указанным UUID существует, но данные отсутствуют, сообщение об ошибке")
    void whenResourceExistButFileIsNotExists_thenGetError() {
        Mono<StorageResource> filePathForUUID = storage.getFileDataByUUID(Mono.just(FileConstants.FROG.getUuid()));

        StepVerifier.create(filePathForUUID)
                .expectError(ResourceReadingException.class)
                .verify();
    }

    @Test
    @Sql(scripts = "/scripts/cleanup_resource.sql")
    @DisplayName("Сохранение одного файла")
    void whenSaveResource_thenGetUUIDAndResourceExists() throws IOException, URISyntaxException {
        Metadata metadata = new Metadata(Bucket.PRODUCT, Category.IMAGE);
        Mono<UUID> savedResourceUUID = storage.createResourceAndGetUUID(
                Mono.just(metadata),
                Mono.just(createFilePart(ContentType.IMAGE_PNG, FileConstants.FROG)));

        StepVerifier.create(savedResourceUUID)
                .expectNextMatches(Objects::nonNull)
                .expectComplete()
                .verify();

        Iterable<StorageResource> all = resourceRepository.findAll();

        Assertions.assertThat(all).hasSize(1);
        StorageResource actual = all.iterator().next();

        Assertions.assertThat(actual.getId()).isEqualTo(1L);
        Assertions.assertThat(actual.getCreateAt()).isNotNull();
        Assertions.assertThat(actual.getUpdateAt()).isNotNull();
        Assertions.assertThat(actual.getCategory()).isEqualTo(Category.IMAGE);
        Assertions.assertThat(actual.getBucket()).isEqualTo(Bucket.PRODUCT);
        Assertions.assertThat(actual.getContentType()).isEqualTo(MediaType.IMAGE_PNG.toString());
        Assertions.assertThat(actual.getTitle()).isEqualTo(FileConstants.FROG.getSourceName());
        Assertions.assertThat(actual.getUuid()).isNotNull();
        Assertions.assertThat(actual.getPath()).isNotNull();

        Assertions.assertThat(Files.exists(storagePath
                .resolve(actual.getBucket().toString().toLowerCase())
                .resolve(actual.getPath()))).isTrue();
    }


    @Test
    @Sql(scripts = "/scripts/cleanup_resource.sql")
    @DisplayName("Сохранение нескольких файлов")
    void whenSaveResources_thenGetUUIDListAndCheckResourceExists() throws IOException, URISyntaxException {
        Metadata metadata = new Metadata(Bucket.COMMENT, Category.IMAGE);

        Mono<List<UUID>> savedResourceUUID = storage.createResourceListAndGetUUIDs(
                Mono.just(metadata),
                Flux.just(
                        createFilePart(ContentType.IMAGE_PNG, FileConstants.FROG),
                        createFilePart(ContentType.IMAGE_PNG, FileConstants.LIZARD),
                        createFilePart(ContentType.IMAGE_PNG, FileConstants.SNAKE)));

        StepVerifier.create(savedResourceUUID)
                .expectNextMatches(Objects::nonNull)
                .expectComplete()
                .verify();

        Iterable<StorageResource> all = resourceRepository.findAll();

        Assertions.assertThat(all).hasSize(3);
        all.forEach(actual -> {
            Assertions.assertThat(actual.getId()).isBetween(1L, 3L);
            Assertions.assertThat(actual.getCreateAt()).isNotNull();
            Assertions.assertThat(actual.getUpdateAt()).isNotNull();
            Assertions.assertThat(actual.getCategory()).isEqualTo(Category.IMAGE);
            Assertions.assertThat(actual.getBucket()).isEqualTo(Bucket.COMMENT);
            Assertions.assertThat(actual.getContentType()).isEqualTo(MediaType.IMAGE_PNG.toString());
            Assertions.assertThat(actual.getTitle()).isIn(FileConstants.FROG.getSourceName(),
                    FileConstants.LIZARD.getSourceName(), FileConstants.SNAKE.getSourceName());
            Assertions.assertThat(actual.getUuid()).isNotNull();
            Assertions.assertThat(actual.getPath()).isNotNull();

            Assertions.assertThat(Files.exists(storagePath
                    .resolve(actual.getBucket().toString().toLowerCase())
                    .resolve(actual.getPath()))).isTrue();
        });
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_resource.sql",
            "/scripts/insert_resource.sql"})
    @DisplayName("Успешное удаление ресурса")
    void whenDeleteResourceIsSuccessful_thenReturnEmpty() throws IOException {
        prepareTestDirectory(storagePath);

        Mono<Void> mono = storage.deleteResourceByUUID(Mono.just(FileConstants.FROG.getUuid()));

        StepVerifier.create(mono)
                .expectComplete()
                .verify();

        Assertions.assertThat(resourceRepository.findByUuid(UUID.fromString(FileConstants.FROG.getUuid())))
                        .isEmpty();

        Assertions.assertThat(isFilePresentRecursively(storagePath, FileConstants.FROG)).isFalse();
    }

    @Test
    @Sql(scripts = "/scripts/cleanup_resource.sql")
    @DisplayName("Попытка удаления ресурса, ресурс отсутствует")
    void whenResourceIsAbsent_thenReturnEmpty() throws IOException {
        Mono<Void> mono = storage.deleteResourceByUUID(Mono.just(FileConstants.FROG.getUuid()));

        StepVerifier.create(mono)
                .expectError(ResourceNotFoundException.class)
                .verify();

        Assertions.assertThat(resourceRepository.findByUuid(UUID.fromString(FileConstants.FROG.getUuid())))
                .isEmpty();

        Assertions.assertThat(isFilePresentRecursively(storagePath, FileConstants.FROG)).isFalse();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_resource.sql",
            "/scripts/insert_resource.sql"})
    @DisplayName("Ресурс присутствует, но файл отсутствует, вернуть ошибку чтения, ресурс НЕ удалять")
    void whenResourceIsAbsent_thenReturnNoContent() throws IOException {
        Mono<Void> mono = storage.deleteResourceByUUID(Mono.just(FileConstants.FROG.getUuid()));

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();

        Assertions.assertThat(resourceRepository.findByUuid(UUID.fromString(FileConstants.FROG.getUuid())))
                .isPresent();

        Assertions.assertThat(isFilePresentRecursively(storagePath, FileConstants.FROG)).isFalse();
    }
}