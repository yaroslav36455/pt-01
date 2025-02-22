package com.tyv.storageservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@TestConfiguration
public class TestcontainersS3AWSConfiguration {

    @Bean
    LocalStackContainer awsContainer() {
        LocalStackContainer awsContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withServices(LocalStackContainer.Service.S3);

        awsContainer.start();
        return awsContainer;
    }

    @Bean
    public S3AsyncClient s3AsyncClient(LocalStackContainer awsContainer) {
        return S3AsyncClient.builder()
                .endpointOverride(awsContainer.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.of(awsContainer.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.builder()
                                .accessKeyId(awsContainer.getAccessKey())
                                .secretAccessKey(awsContainer.getSecretKey())
                        .build()))
                .build();
    }
}
