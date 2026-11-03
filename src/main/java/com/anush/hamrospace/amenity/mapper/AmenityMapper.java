package com.anush.hamrospace.amenity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.anush.hamrospace.amenity.dto.request.AmenityRequest;
import com.anush.hamrospace.amenity.dto.response.AmenityResponse;
import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.common.mapper.MapStructMapperHelper;
import com.anush.hamrospace.common.storage.util.DefaultImageMapper;

@Mapper(componentModel = "spring",uses = {DefaultImageMapper.class,MapStructMapperHelper.class},nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AmenityMapper {
    
AmenityResponse toResponse(Amenity amenity);

Amenity toEntity(AmenityRequest request);

void updateEntityFromDto(AmenityRequest request, @MappingTarget Amenity entity);
}
