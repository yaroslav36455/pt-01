package com.tyv.customerservice.config;

import com.tyv.customerservice.client.DocumentClient;
import com.tyv.customerservice.dto.ExceptionDto;
import com.tyv.customerservice.exception.DocumentResourceConsistencyException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories
public class ProjectConfiguration {

    @Value("${service.document.url}")
    private String documentServiceUrl;

    @Bean
    public DocumentClient documentClient(LoadBalancedExchangeFilterFunction loadBalancer,
                                         @Qualifier("documentResourceNotFoundFilter") ExchangeFilterFunction documentResourceNotFoundFilter) {
        WebClient webClient = WebClient.builder()
                .filter(loadBalancer)
                .filter(documentResourceNotFoundFilter)
                .baseUrl(documentServiceUrl)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(DocumentClient.class);
    }

    @Bean
    public ExchangeFilterFunction documentResourceNotFoundFilter() {
        return (request, next) -> next.exchange(request)
                .flatMap(clientResponse ->
                        clientResponse.statusCode().value() == HttpStatus.NOT_FOUND.value()
                                ? clientResponse.bodyToMono(ExceptionDto.class)
                                .flatMap(errorDto ->
                                        Mono.error(new DocumentResourceConsistencyException(request.url(), errorDto)))
                                : Mono.just(clientResponse));
    }

    @Bean
    public ReactiveAuditorAware<String> auditorProvider() {
        return () -> Mono.just("system");
    }
}
