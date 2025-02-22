package com.tyv.storageservice.service;

import com.tyv.storageservice.entity.StorageResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@Profile("localStorage")
@RequiredArgsConstructor
public class FileStorageLocal implements FileStorage {
    @Value("${storage.path}")
    private String STORAGE_PATH;

    @Override
    public Mono<StorageResource> getData(StorageResource resource) {
        return Mono.fromCallable(() -> Files.readAllBytes(getRelativePath(resource)))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(resource::setData)
                .thenReturn(resource);
    }

    @Override
    public Mono<Void> save(StorageResource storageResource, FilePart filePart) {
        Path path = getRelativePath(storageResource);

        try {
            prepareDirectory(path.getParent());
        } catch (IOException e) {
            return Mono.error(e);
        }

        return filePart.transferTo(path);
    }

    @Override
    public Mono<Boolean> deleteFile(StorageResource resource) {
        return Mono.just(getRelativePath(resource))
                .map(Path::toFile)
                .map(File::delete);
    }

    private void prepareDirectory(Path path) throws IOException {
        File dir = path.toFile();

        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("Directory [{}] has been created", path.toFile().getCanonicalPath());
            } else {
                log.error("Directory [{}] could not be created", path.toFile().getCanonicalPath());
            }
        }
    }

    private Path getRelativePath(StorageResource storageResource) {
        return Path.of(STORAGE_PATH,
                storageResource.getBucket().toString().toLowerCase(),
                storageResource.getPath());
    }
}
