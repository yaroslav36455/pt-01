package com.tyv.productservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.tyv.productservice.dto.ProductDtoView.RequestCreate;
import com.tyv.productservice.dto.ProductDtoView.RequestUpdate;
import com.tyv.productservice.dto.ProductDtoView.Response;
import com.tyv.productservice.entity.ProductStatus;
import com.tyv.productservice.util.Formatter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDto(
        @JsonView({RequestUpdate.class, Response.class})
        String id,

        @JsonView(Response.class)
        @JsonFormat(pattern = Formatter.DATE_TIME_PATTERN_PUBLIC)
        LocalDateTime createdAt,

        @JsonView(Response.class)
        @JsonFormat(pattern = Formatter.DATE_TIME_PATTERN_PUBLIC)
        LocalDateTime updatedAt,

        @JsonView({RequestCreate.class, RequestUpdate.class, Response.class})
        String code,

        @JsonView({RequestCreate.class, RequestUpdate.class, Response.class})
        ProductStatus status,

        @JsonView({RequestCreate.class, RequestUpdate.class, Response.class})
        String group,

        @JsonView({RequestCreate.class, RequestUpdate.class, Response.class})
        String title,

        @JsonView({RequestCreate.class, RequestUpdate.class, Response.class})
        String description,

        @JsonView({RequestCreate.class, RequestUpdate.class, Response.class})
        BigDecimal price,

        @JsonView({RequestCreate.class, RequestUpdate.class, Response.class})
        Integer stock
) {}
