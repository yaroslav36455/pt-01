package com.tyv.customerservice.mapper;


import com.tyv.customerservice.dto.AddressDto;
import com.tyv.customerservice.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AddressMapper {

    AddressDto toDto(Address address);

    Address toEntity(AddressDto addressDto);

    void toEntityUpdate(@MappingTarget Address address, AddressDto addressDto);
}
