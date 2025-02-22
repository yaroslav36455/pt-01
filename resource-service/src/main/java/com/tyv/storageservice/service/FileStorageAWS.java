package com.tyv.storageservice.service;

import com.tyv.storageservice.entity.StorageResource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

@Service
@Slf4j
@Profile({"awsStorage", "awsStorageTest"})
@RequiredArgsConstructor
public class FileStorageAWS implements FileStorage {

    private final S3RequestFactory requestFactory;
    private final S3AsyncClient s3Client;
    private final ConcurrentSkipListSet<String> bucketNames = new ConcurrentSkipListSet<>();

    @PostConstruct
    public void init() {
        s3Client.listBuckets()
                .thenAccept(response -> response.buckets().forEach(bucket -> {
                    bucketNames.add(bucket.name());
                }))
                .join();
    }

    @Override
    public Mono<StorageResource> getData(StorageResource resource) {
        GetObjectRequest getObjectRequest = requestFactory.getObject(resource);
        return Mono.fromCompletionStage(s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes())
                .thenApply(BytesWrapper::asByteArrayUnsafe)
        )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(resource::setData)
                .thenReturn(resource);
    }


    @Override
    public Mono<Void> save(StorageResource storageResource, FilePart filePart) {
        PutObjectRequest request = requestFactory.putObject(storageResource);
        return Mono.fromCompletionStage(() -> prepareBucket(request.bucket()))
                .thenMany(filePart.content())
                .reduce(DataBuffer::write)
                .flatMap((Function<DataBuffer, Mono<Void>>) dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);

                    return Mono.fromCompletionStage(() -> s3Client.putObject(request, AsyncRequestBody.fromBytes(bytes)))
                            .then();
                });
    }

    @Override
    public Mono<Boolean> deleteFile(StorageResource resource) {
        return Mono.fromCompletionStage(() -> s3Client.deleteObject(requestFactory.deleteObject(resource))
                .thenCompose(response -> isObjectAbsent(resource)));
    }

    private CompletableFuture<Boolean> isObjectAbsent(StorageResource resource) {
        return s3Client.headObject(requestFactory.headObject(resource))
                .handle((headObjectResponse, throwable) ->
                        Objects.nonNull(throwable) && throwable.getCause() instanceof NoSuchKeyException);
    }

    private CompletableFuture<Void> prepareBucket(String name) {
        if (!bucketNames.contains(name)) {
            return s3Client.createBucket(requestFactory.createBucket(name))
                    .thenRunAsync(() -> {
                        log.info("Bucket '{}' has been created", name);
                        bucketNames.add(name);
                    })
                    .exceptionallyAsync(throwable -> {
                        if (throwable instanceof CompletionException
                                && throwable.getCause() instanceof BucketAlreadyExistsException) {
                            log.warn("Bucket '{}' already exists, skipping", name);
                            bucketNames.add(name);
                            return null;
                        }

                        throw new CompletionException(throwable.getCause());
                    });
        }

        return CompletableFuture.completedFuture(null);
    }
}
