package com.tyv.customerservice.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DocumentClient {

    @GetExchange("/api/resource/{uuid}")
    Mono<ResponseEntity<byte[]>> getDocument(@PathVariable("uuid") UUID documentId);
}
