package com.tyv.customerservice.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.tyv.customerservice.TestcontainersConfiguration;
import com.tyv.customerservice.WireMockConfig;
import com.tyv.customerservice.WireMockConfiguration;
import com.tyv.customerservice.dto.DocumentDto;
import com.tyv.customerservice.exception.CustomerNotFoundException;
import com.tyv.customerservice.exception.DocumentNotFoundException;
import com.tyv.customerservice.exception.DocumentResourceConsistencyException;
import com.tyv.customerservice.util.DocumentUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import reactor.test.StepVerifier;


@Import({TestcontainersConfiguration.class, WireMockConfiguration.class})
@AutoConfigureWireMock(port = 0)
@SpringBootTest
public class CustomerServiceImplWithWireMockTest {

    @Autowired
    private CustomerService customerService;


    @DynamicPropertySource
    static public void wiremockProperties(DynamicPropertyRegistry registry) {
        registry.add("service.document.url", WireMockConfig::getBaseUrl);
    }

    @AfterEach
    public void cleanUp() {
        WireMock.reset();
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Вернуть document, если document был найден")
    void whenGetCustomerDocumentByCustomerId_thenReturnDocument() {
        long customerId = 1L;
        DocumentDto responseDto = DocumentUtil.createResponseDto();

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/resource/.*"))
                        .willReturn(WireMock
                                .aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                .withHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(responseDto.getLength()))
                                .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + responseDto.getTitle() + "\"")
                                .withBody(responseDto.getData())));

        StepVerifier.create(customerService.getDocumentByCustomerId(customerId))
                .expectNext(responseDto)
                .verifyComplete();

        WireMock.verify(1, RequestPatternBuilder.allRequests());
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql"})
    @DisplayName("Если customer не были найдены, то вернуть исключение CustomerNotFoundException")
    void whenGetCustomerDocumentByCustomerIdAndCustomerWasNotFound_thenReturnException() {
        long customerId = 1L;
        DocumentDto responseDto = DocumentUtil.createResponseDto();

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/resource/.*"))
                        .willReturn(WireMock
                                .aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                .withHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(responseDto.getLength()))
                                .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + responseDto.getTitle() + "\"")
                                .withBody(responseDto.getData())));

        StepVerifier.create(customerService.getDocumentByCustomerId(customerId))
                .expectError(CustomerNotFoundException.class)
                .verify();

        WireMock.verify(0, RequestPatternBuilder.allRequests());
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer_without_document.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Если document не были найдены, то вернуть исключение DocumentNotFoundException")
    void whenGetCustomerDocumentByCustomerIdAndDocumentWasNotFound_thenReturnException() {
        long customerId = 1L;
        DocumentDto responseDto = DocumentUtil.createResponseDto();

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/resource/.*"))
                        .willReturn(WireMock
                                .aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                .withHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(responseDto.getLength()))
                                .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + responseDto.getTitle() + "\"")
                                .withBody(responseDto.getData())));

        StepVerifier.create(customerService.getDocumentByCustomerId(customerId))
                .expectError(DocumentNotFoundException.class)
                .verify();

        WireMock.verify(0, RequestPatternBuilder.allRequests());
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Если document uuid был найден document ресурс не был найден, то вернуть ошибку")
    void whenGetCustomerDocumentByCustomerIdDocumentResourceWasNotFound_thenReturnException() {
        long customerId = 1L;

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/resource/.*"))
                        .willReturn(WireMock
                                .aResponse()
                                .withStatus(HttpStatus.NOT_FOUND.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("exception-response.json")));

        StepVerifier.create(customerService.getDocumentByCustomerId(customerId))
                .expectError(DocumentResourceConsistencyException.class)
                .verify();

        WireMock.verify(1, RequestPatternBuilder.allRequests());
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_customer.sql",
            "/scripts/cleanup_address.sql",
            "/scripts/insert_customer.sql",
            "/scripts/insert_address.sql"})
    @DisplayName("Если случилось нечто противоестественное с клиентом, то вернуть ошибку")
    void whenGetCustomerDocumentByCustomerIdAndDocumentClientErrorHappenedWasNotFound_thenReturnException() {
        long customerId = 1L;

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/resource/.*"))
                        .willReturn(WireMock
                                .aResponse()
                                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody("Some error message")));

        StepVerifier.create(customerService.getDocumentByCustomerId(customerId))
                .expectError(RuntimeException.class)
                .verify();

        WireMock.verify(1, RequestPatternBuilder.allRequests());
    }
}
