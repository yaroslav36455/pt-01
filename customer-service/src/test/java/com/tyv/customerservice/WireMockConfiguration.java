package com.tyv.customerservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class WireMockConfiguration {

    @Bean
    public LoadBalancedExchangeFilterFunction loadBalancer() {
        return (request, next) -> next.exchange(request);
    }
}
