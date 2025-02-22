package com.tyv.storageservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
@Profile("awsStorage")
public class AWSConfiguration {

    public static String REGION;

    @Value("${AWS_REGION}")
    public void setRegion(String region) {
        REGION = region;
    }

    @Bean
    public S3AsyncClient s3AsyncClient() {
        return S3AsyncClient.builder()
                .region(Region.of(REGION))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }
}
