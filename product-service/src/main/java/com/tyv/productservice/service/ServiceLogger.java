package com.tyv.productservice.service;

import com.tyv.productservice.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ServiceLogger {

    public static void productCreated(ProductDto productDto) {
        log.info("Created new product with id [{}]", productDto.id());
    }

    public static void productUpdated(ProductDto productDto) {
        log.info("Updated product with id [{}]", productDto.id());
    }

    public static void productSavingError(Throwable exception) {
        log.warn("Product saving error [{}]", exception.getMessage());
    }
}
