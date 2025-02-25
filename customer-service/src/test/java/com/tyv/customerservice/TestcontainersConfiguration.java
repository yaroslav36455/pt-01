package com.tyv.customerservice;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

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

	@Bean
	@Primary
	public TransactionManager transactionManagerForTest(ConnectionFactory connectionFactory) {
		return new R2dbcTransactionManager(connectionFactory);
	}

	@Bean
	@LiquibaseDataSource
	public DataSource dataSource(PostgreSQLContainer<?> postgresContainer) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl(postgresContainer.getJdbcUrl());
		dataSource.setUsername(postgresContainer.getUsername());
		dataSource.setPassword(postgresContainer.getPassword());
		return dataSource;
	}
}
