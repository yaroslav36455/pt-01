package com.tyv.storageservice.controller;

import com.tyv.storageservice.dto.ResponseExceptionDto;
import com.tyv.storageservice.entity.StorageResource;
import com.tyv.storageservice.enums.Bucket;
import com.tyv.storageservice.enums.Category;
import com.tyv.storageservice.exception.ResourceNotFoundException;
import com.tyv.storageservice.service.Storage;
import com.tyv.storageservice.util.FileConstants;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.tyv.storageservice.util.LocalStorageResourceUtil.createFilePart;

@WebFluxTest(controllers = ResourceController.class)
class ResourceControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private Storage storage;

    @Test
    @DisplayName("Возвращать данные и статус 200, если ресурс найден")
    void whenGetResourceByUUIDAndResourceFound_thenReturnResourceData() {
        String requestUUID = "e45331f2-5941-4b35-baa0-20e01f016f1e";
        String contentType = "text/plain";
        String filename = "test_file.txt";
        String data = "Test data response";
        StorageResource resource = StorageResource.builder()
                .contentType(contentType)
                .title(filename)
                .data(data.getBytes())
                .build();

        Mockito.doReturn(Mono.just(resource))
                .when(storage)
                .getFileDataByUUID(Mockito.any());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/resource/{uuid}")
                        .build(requestUUID))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentDisposition(ContentDisposition.attachment().filename(filename).build())
                .expectHeader().contentType(contentType)
                .expectHeader().contentLength(data.getBytes().length)
                .expectBody(String.class)
                .isEqualTo(data);

        Mockito.verify(storage, Mockito.only())
                .getFileDataByUUID(Mockito.argThat(mono -> Objects.equals(mono.block(), requestUUID)));
    }

    @Test
    @DisplayName("Возвращать статус 404, timestamp и сообщение, если ресурс НЕ найден")
    void whenGetResourceByUUIDAndResourceNotFound_thenReturnMessage() {
        String requestUUID = "e45331f2-5941-4b35-baa0-20e01f016f1e";

        Mockito.doReturn(Mono.error(new ResourceNotFoundException("Resource by uuid [" + requestUUID + "] not found")))
                .when(storage)
                .getFileDataByUUID(Mockito.any());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/resource/{uuid}")
                        .build(requestUUID))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ResponseExceptionDto.class)
                        .consumeWith(response -> {
                            ResponseExceptionDto responseBody = response.getResponseBody();
                            Assertions.assertThat(responseBody)
                                    .isNotNull();
                            Assertions.assertThat(responseBody)
                                    .extracting(ResponseExceptionDto::getErrorMessage)
                                    .isEqualTo("Resource by uuid [" + requestUUID + "] not found");
                            Assertions.assertThat(responseBody)
                                    .extracting(ResponseExceptionDto::getTimestamp)
                                    .isNotNull();
                        });

        Mockito.verify(storage, Mockito.only())
                .getFileDataByUUID(Mockito.argThat(mono -> Objects.equals(mono.block(), requestUUID)));
    }

    @Test
    @DisplayName("Возвращать статус 201 и UUID когда ресурс создан успешно")
    void whenCreateResourceSuccessful_thenReturnUUID() throws IOException, URISyntaxException {
        FilePart multipartFile = createFilePart(ContentType.TEXT_PLAIN, FileConstants.MESSAGE);
        Mono<FilePart> filePartMono = Mono.just(multipartFile);
        UUID responseUuid = UUID.randomUUID();

        Mockito.doReturn(Mono.just(responseUuid))
                .when(storage)
                .createResourceAndGetUUID(Mockito.any(), Mockito.any());

        MultipartBodyBuilder multipartData = new MultipartBodyBuilder();
        multipartData.asyncPart("file", filePartMono, FilePart.class).contentType(MediaType.MULTIPART_FORM_DATA);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/resource/upload")
                        .queryParam("category", Category.TEXT)
                        .queryParam("bucket", Bucket.COMMENT)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData.build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UUID.class)
                .isEqualTo(responseUuid);
    }

    @Test
    @DisplayName("Возвращать статус 201 и UUID когда несколько ресурсов созданы успешно")
    void whenCreateResourceListSuccessful_thenReturnListOfUUID() throws IOException, URISyntaxException {
        FilePart filePartFirst = createFilePart(ContentType.IMAGE_PNG, FileConstants.FROG);
        FilePart filePartSecond = createFilePart(ContentType.IMAGE_PNG, FileConstants.SNAKE);
        List<UUID> responseUUIDList = List.of(UUID.randomUUID(), UUID.randomUUID());

        Mockito.doReturn(Mono.just(responseUUIDList))
                .when(storage)
                .createResourceListAndGetUUIDs(Mockito.any(), Mockito.any());

        MultipartBodyBuilder multipartData = new MultipartBodyBuilder();
        multipartData.part("file", filePartFirst).contentType(MediaType.MULTIPART_FORM_DATA);
        multipartData.part("file", filePartSecond).contentType(MediaType.MULTIPART_FORM_DATA);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/resource/uploadList")
                        .queryParam("category", Category.IMAGE)
                        .queryParam("bucket", Bucket.PRODUCT)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData.build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(new ParameterizedTypeReference<List<UUID>>() {
                })
                .value(responseList -> Assertions.assertThat(responseList)
                        .containsExactlyInAnyOrderElementsOf(responseUUIDList));
    }

    @Test
    @DisplayName("Успешное удаление ресурса, возвращается статус 204")
    void whenDeletingResourceIsSuccessful_thenReturnNoContentStatus() {
        String requestUUID = "e45331f2-5941-4b35-baa0-20e01f016f1e";

        Mockito.doReturn(Mono.empty())
                .when(storage)
                .deleteResourceByUUID(Mockito.any());

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/resource/{uuid}")
                        .build(requestUUID))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

        Mockito.verify(storage, Mockito.only())
                .deleteResourceByUUID(Mockito.argThat(mono -> Objects.equals(mono.block(), requestUUID)));
    }

    @Test
    @DisplayName("Ресурс отсутствует, возвращается статус 204")
    void whenDeletingResourceIsNotFound_thenReturnNoContentStatus() {
        String requestUUID = "e45331f2-5941-4b35-baa0-20e01f016f1e";

        Mockito.doReturn(Mono.error(new ResourceNotFoundException("Resource by uuid [" + requestUUID + "] not found")))
                .when(storage)
                .deleteResourceByUUID(Mockito.any());

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/resource/{uuid}")
                        .build(requestUUID))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

        Mockito.verify(storage, Mockito.only())
                .deleteResourceByUUID(Mockito.argThat(mono -> Objects.equals(mono.block(), requestUUID)));
    }
}