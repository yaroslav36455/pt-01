package com.tyv.productservice.service;

import com.tyv.productservice.dto.ProductDto;
import com.tyv.productservice.mapper.ProductMapper;
import com.tyv.productservice.ropository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Flux<ProductDto> getProducts() {
        return productRepository.findAll()
                .map(productMapper::toDto);
    }

    public Mono<ProductDto> getProduct(String id) {
        return productRepository.findById(id)
                .map(productMapper::toDto);
    }

    public Mono<ProductDto> create(ProductDto product) {
        return productRepository.save(productMapper.toProduct(product))
                .map(productMapper::toDto)
                .doOnSuccess(ServiceLogger::productCreated)
                .doOnError(ServiceLogger::productSavingError);
    }

    public void delete(String id) {
        productRepository.deleteById(id).subscribe();
    }

    public Mono<ProductDto> update(ProductDto productDto) {
        return productRepository.findById(productDto.id())
                .flatMap(found -> {
                    productMapper.updateProduct(productDto, found);
                    return productRepository.save(found);
                })
                .map(productMapper::toDto)
                .doOnSuccess(ServiceLogger::productUpdated);
    }
}
