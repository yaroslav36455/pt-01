package com.tyv.storageservice.service;

import com.tyv.storageservice.config.AWSConfiguration;
import com.tyv.storageservice.entity.StorageResource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class S3RequestFactory {

    public GetObjectRequest getObject(StorageResource resource) {
        return GetObjectRequest.builder()
                .bucket(resource.getBucket().getWithUUID())
                .key(resource.getPath())
                .build();
    }

    public PutObjectRequest putObject(StorageResource resource) {
        return PutObjectRequest.builder()
                .bucket(resource.getBucket().getWithUUID())
                .key(resource.getPath())
                .build();
    }

    public DeleteObjectRequest deleteObject(StorageResource resource) {
        return DeleteObjectRequest.builder()
                .bucket(resource.getBucket().getWithUUID())
                .key(resource.getPath())
                .build();
    }

    public HeadObjectRequest headObject(StorageResource resource) {
        return HeadObjectRequest.builder()
                .bucket(resource.getBucket().getWithUUID())
                .key(resource.getPath())
                .build();
    }

    public CreateBucketRequest createBucket(String bucketName) {
        return CreateBucketRequest.builder()
                .bucket(bucketName)
                .createBucketConfiguration(CreateBucketConfiguration.builder()
                        .locationConstraint(AWSConfiguration.REGION)
                        .build())
                .build();
    }
}
