package com.tyv.productservice.mapper;

import com.tyv.productservice.dto.ProductDto;
import com.tyv.productservice.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductMapper {

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", defaultExpression = "java(com.tyv.productservice.entity.ProductStatus.ACTIVE)")
    Product toProduct(ProductDto productDto);
    ProductDto toDto(Product product);

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProduct(ProductDto productSource, @MappingTarget Product productTarget);
}
