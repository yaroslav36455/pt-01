package com.tyv.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document("product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    private String id;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Indexed(unique = true)
    private String code;
    private ProductStatus status;
    private ProductGroup group;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stock;
}
