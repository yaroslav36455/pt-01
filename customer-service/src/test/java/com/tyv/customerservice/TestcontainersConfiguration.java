package com.tyv.customerservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Value("${testcontainers.postgres.version}")
	String postgresVersion;

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:" + postgresVersion));
		container.start();
		return container;
	}
}
