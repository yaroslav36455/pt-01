package com.tyv.storageservice.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomFilePart implements FilePart {
    private final String filename;
    private final HttpHeaders headers;
    private final byte[] buffer;

    public CustomFilePart(@NotNull String filename, @NotNull HttpHeaders headers, byte[] buffer) {
        this.filename = filename;
        this.headers = headers;
        this.buffer = buffer;
    }

    @Override
    public @NotNull String filename() {
        return filename;
    }

    @Override
    public @NotNull Mono<Void> transferTo(@NotNull Path dest) {
        return Mono.fromRunnable(() -> {
            try {
                Files.write(dest, buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public @NotNull String name() {
        return filename;
    }

    @Override
    public @NotNull HttpHeaders headers() {
        return headers;
    }

    @Override
    public @NotNull Flux<DataBuffer> content() {
        return Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(buffer));
    }

    @Override
    public @NotNull Mono<Void> delete() {
        return Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
