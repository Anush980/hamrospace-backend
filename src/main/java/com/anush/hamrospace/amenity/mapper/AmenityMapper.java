package com.anush.hamrospace.amenity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.anush.hamrospace.amenity.dto.request.AmenityRequest;
import com.anush.hamrospace.amenity.dto.response.AmenityResponse;
import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.common.mapper.MapStructMapperHelper;
import com.anush.hamrospace.common.storage.util.DefaultImageMapper;

@Mapper(componentModel = "spring",uses = {DefaultImageMapper.class,MapStructMapperHelper.class},nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AmenityMapper {
    
@Mapping(target = "isFeatured",source = "featured")
@Mapping(target = "isActive",source = "active")
AmenityResponse toResponse(Amenity amenity);

@Mapping(source = "isFeatured",target = "featured")
@Mapping(source = "isActive",target = "active")
Amenity toEntity(AmenityRequest request);
}
