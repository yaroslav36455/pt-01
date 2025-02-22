package com.tyv.productservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.tyv.productservice.dto.ErrorResponseDto;
import com.tyv.productservice.dto.ProductDto;
import com.tyv.productservice.dto.ProductDtoView.RequestCreate;
import com.tyv.productservice.dto.ProductDtoView.RequestUpdate;
import com.tyv.productservice.dto.ProductDtoView.Response;
import com.tyv.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @JsonView(Response.class)
    public Flux<ProductDto> getAllProducts() {
        return productService.getProducts();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @JsonView(Response.class)
    public Mono<ProductDto> getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Response.class)
    public Mono<ProductDto> create(@RequestBody @JsonView(RequestCreate.class) ProductDto product) {
        return productService.create(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        productService.delete(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @JsonView(Response.class)
    public Mono<ProductDto> update(@RequestBody @JsonView(RequestUpdate.class) ProductDto productDto) {
        return productService.update(productDto);
    }


    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponseDto> handleException(DuplicateKeyException exception) {
        return Mono.just(new ErrorResponseDto(exception.getMessage()));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponseDto> handleException(Throwable exception) {
        return Mono.just(new ErrorResponseDto(exception.getMessage()));
    }
}
