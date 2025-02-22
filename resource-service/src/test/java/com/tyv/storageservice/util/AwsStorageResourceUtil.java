package com.tyv.storageservice.util;

import com.tyv.storageservice.enums.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.tyv.storageservice.enums.Bucket.PRODUCT;
import static com.tyv.storageservice.enums.Bucket.USER;
import static com.tyv.storageservice.util.FileConstants.*;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class AwsStorageResourceUtil {
    private final S3AsyncClient client;

    public void prepareTestBuckets() {
        createBucket(PRODUCT)
                .thenCompose(response -> putObject(PRODUCT, FROG))
                .thenCompose(response -> putObject(PRODUCT, SNAKE))
                .thenCompose(response -> createBucket(USER))
                .thenCompose(response -> putObject(USER, LIZARD))
                .thenCompose(response -> putObject(USER, MESSAGE))
                .join();
    }

    public void dropTestBuckets() {

        dropObjects(PRODUCT)
                .thenCompose(response -> dropBucket(PRODUCT))
                .thenCompose(response -> dropObjects(USER))
                .thenCompose(response -> dropBucket(USER))
                .join();
    }

    public CompletableFuture<CreateBucketResponse> createBucket(Bucket bucket) {
        return client.createBucket(CreateBucketRequest.builder()
                .bucket(bucket.getWithUUID())
                .build());
    }

    public CompletableFuture<DeleteBucketResponse> dropBucket(Bucket bucket) {
        return client.deleteBucket(DeleteBucketRequest.builder()
                .bucket(bucket.getWithUUID())
                .build())
                .handle(this::handleException);
    }

    private CompletableFuture<PutObjectResponse> putObject(Bucket bucket, FileConstants constant) {
        try {
            return client.putObject(PutObjectRequest.builder()
                            .bucket(bucket.getWithUUID())
                            .key(buildKeyPath(constant))
                            .build(),
                    AsyncRequestBody.fromBytes(readFile(constant)));
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    private CompletableFuture<DeleteObjectsResponse> dropObjects(Bucket bucket) {
        ListObjectsV2Request.builder()
                .bucket(bucket.getWithUUID())
                .build();
        return client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket.getWithUUID())
                .build())
                .thenApply(ListObjectsV2Response::contents)
                .thenApply(s3Objects -> s3Objects.stream()
                        .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                        .collect(Collectors.toList()))
                .thenCompose(objectIdentifiers -> {
                    if (!objectIdentifiers.isEmpty()) {
                        return client.deleteObjects(DeleteObjectsRequest.builder()
                                .bucket(bucket.getWithUUID())
                                .delete(Delete.builder().objects(objectIdentifiers).build())
                                .build());
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .handle(this::handleException);
    }

    public <T> T handleException(T deleteResponse, Throwable throwable) {
        if (isNull(throwable)) {
            return deleteResponse;
        } else if (throwable instanceof CompletionException &&
                (throwable.getCause() instanceof NoSuchKeyException || (throwable.getCause() instanceof NoSuchBucketException))) {
            return null;
        }

        throw new CompletionException(throwable);
    }

    private String buildKeyPath(FileConstants constant) {
        return Path.of(LocalDate.now().toString(), constant.getNameWithUUID()).toString();
    }

    private byte[] readFile(FileConstants constant) throws IOException {
        return Files.readAllBytes(Path.of(Objects.requireNonNull(
                LocalStorageResourceUtil.class.getResource(constant.getSourcePath())).getPath()));
    }

    public boolean isExists(Bucket bucket, FileConstants constant) {
        return isExists(bucket, Path.of(LocalDate.now().toString(), constant.getNameWithUUID()));
    }

    public boolean isExists(Bucket bucket, Path path) {
        return client.headObject(HeadObjectRequest.builder()
                .bucket(bucket.getWithUUID())
                .key(path.toString())
                .build())
                .handle((headObjectResponse, throwable) -> {
                    if (isNull(throwable)) {
                        return true;
                    } else if (throwable.getCause() instanceof NoSuchKeyException exception
                            && exception.statusCode() == HttpStatus.NOT_FOUND.value()) {
                        return false;
                    }

                    throw new CompletionException(throwable.getCause());
                })
                .join();
    }
}
