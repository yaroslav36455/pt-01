package com.tyv.storageservice.controller;

import com.tyv.storageservice.model.Metadata;
import com.tyv.storageservice.dto.ResponseExceptionDto;
import com.tyv.storageservice.enums.Bucket;
import com.tyv.storageservice.enums.Category;
import com.tyv.storageservice.exception.ResourceNotFoundException;
import com.tyv.storageservice.service.Storage;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final Storage storage;

    @Operation(
            summary = "Вернуть ресурс",
            description = "Возвращение массива байтов по UUID загруженного раннее файла",
    responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ресурс найден, возвращаются данные",
                    headers = {
                            @Header(name = HttpHeaders.CONTENT_DISPOSITION, description = "Имя файла"),
                            @Header(name = HttpHeaders.CONTENT_LENGTH, description = "Размер файла в байтах"),
                            @Header(name = HttpHeaders.CONTENT_TYPE, description = "Тип файла")},
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ресурс по указанному UUID не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseExceptionDto.class)))
    })
    @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<byte[]>> getFilePath(
            @Parameter(description = "Уникальный идентификатор ресурса", example = "9550a615-edd0-44b0-a2ca-507d6d6f5aeb")
            @PathVariable("uuid") UUID uuid) {
        return storage.getFileDataByUUID(Mono.just(uuid.toString()))
                .map(resource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getTitle() + "\"")
                        .contentLength(resource.getData().length)
                        .contentType(MediaType.parseMediaType(resource.getContentType()))
                        .body(resource.getData()));
    }

    @Operation(
            summary = "Создать ресурс",
            description = "Создать ресурс и получить UUID в ответ. UUID необходим для получения ресурса",
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "Ресурс успешно создан, вернуть UUID",
                    content = @Content(schema = @Schema(type = "string", format = "uuid", example = "9550a615-edd0-44b0-a2ca-507d6d6f5aeb"))))
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> createResourceAndGetUUID(@RequestParam("bucket") Bucket bucket,
                                               @RequestParam("category") Category category,
                                               @RequestPart("file") Mono<FilePart> file) {
        return storage.createResourceAndGetUUID(Mono.just(new Metadata(bucket, category)), file);
    }

    @Operation(
            summary = "Создать список ресурсов",
            description = "Создать список ресурсов и получить список UUID в ответ. UUID необходимы для получения ресурсов",
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "Ресурсы успешно созданы, вернуть список UUID",
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "string", format = "uuid",
                            examples = {"9550a615-edd0-44b0-a2ca-507d6d6f5aeb", "e45331f2-5941-4b35-baa0-10e01f016f1e"})))))
    @PostMapping(value = "/uploadList",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<List<UUID>> createResourceListAndGetUUIDs(@RequestParam("bucket") Bucket bucket,
                                                          @RequestParam("category") Category category,
                                                          @RequestPart("file") Flux<FilePart> file) {
        return storage.createResourceListAndGetUUIDs(Mono.just(new Metadata(bucket, category)), file);
    }

    @Operation(
            summary = "Удалить ресурс",
            description = "Удалить ресурс по UUID",
            responses = @ApiResponse(responseCode = "204", description = "Ресурс успешно удалён"))
    @DeleteMapping(value = "/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteResourceByUUID(
            @Parameter(description = "Уникальный идентификатор ресурса", example = "9550a615-edd0-44b0-a2ca-507d6d6f5aeb")
            @PathVariable("uuid") UUID uuid) {
        return storage.deleteResourceByUUID(Mono.just(uuid.toString()))
                .onErrorResume(throwable -> throwable instanceof ResourceNotFoundException, throwable -> Mono.empty());
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @Hidden
    public Mono<ResponseExceptionDto> resourceException(ResourceNotFoundException exception) {
        return Mono.just(ResponseExceptionDto.builder()
                .timestamp(LocalDateTime.now())
                .errorMessage(exception.getMessage())
                .build());
    }
}
