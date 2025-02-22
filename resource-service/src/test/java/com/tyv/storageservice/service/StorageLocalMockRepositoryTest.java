package com.tyv.storageservice.service;

import com.tyv.storageservice.TestcontainersPostgresConfiguration;
import com.tyv.storageservice.model.Metadata;
import com.tyv.storageservice.entity.StorageResource;
import com.tyv.storageservice.enums.Bucket;
import com.tyv.storageservice.enums.Category;
import com.tyv.storageservice.repository.ResourceRepository;
import com.tyv.storageservice.util.FileConstants;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.UUID;

import static com.tyv.storageservice.util.LocalStorageResourceUtil.*;

@Import(TestcontainersPostgresConfiguration.class)
@SpringBootTest(properties = "spring.profiles.active=localStorage")
public class StorageLocalMockRepositoryTest {
    @Autowired
    private Storage storage;

    @Autowired
    private FileStorage fileStorage;

    @MockitoSpyBean
    private ResourceRepository resourceRepository;


    @Value("${storage.path}")
    private Path storagePath;

    @AfterEach
    void tearDown() throws IOException {
        deleteDirectory(storagePath);
    }

    @Test
    @Sql(scripts = "/scripts/cleanup_resource.sql")
    @DisplayName("Если произошла ошибка сохранения в базу данных, то файл не должен сохраняться")
    void whenDatabaseSavingWithError_thenFileShouldNotBeSaved() throws IOException, URISyntaxException {
        Metadata metadata = new Metadata(Bucket.COMMENT, Category.IMAGE);

        Mockito.doThrow(new RuntimeException("Something went wrong"))
                .when(resourceRepository)
                .save(Mockito.any(StorageResource.class));

        Mono<UUID> savedResourceUUID = storage.createResourceAndGetUUID(
                Mono.just(metadata),
                Mono.just(createFilePart(ContentType.IMAGE_PNG, FileConstants.FROG)));

        StepVerifier.create(savedResourceUUID)
                .expectError()
                .verify();

        Assertions.assertThat(resourceRepository.count()).isZero();
        Assertions.assertThat(isDirectoryEmptyRecursively(storagePath)).isFalse();
    }
}
