package com.tyv.customerservice.mapper;

import com.tyv.customerservice.dto.CustomerDto;
import com.tyv.customerservice.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    CustomerDto toDto(Customer customer);

    @Mapping(target = "address", ignore = true)
    Customer toEntity(CustomerDto dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "address", ignore = true)
    void toEntityUpdate(@MappingTarget Customer target, CustomerDto source);
}
