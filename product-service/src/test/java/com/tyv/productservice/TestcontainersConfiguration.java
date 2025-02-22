package com.tyv.productservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Value("${testcontainers.mongodb.version}")
	String mongoDbVersion;

	@Bean
	@ServiceConnection
	MongoDBContainer mongoDbContainer() {
		System.out.println("MongoDB Version: " + mongoDbVersion);
		return new MongoDBContainer(DockerImageName.parse("mongo:" + mongoDbVersion));
	}

}
