package com.tyv.storageservice;

import org.springframework.boot.SpringApplication;

public class TestResourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(ResourceServiceApplication::main).with(TestcontainersPostgresConfiguration.class).run(args);
	}

}
