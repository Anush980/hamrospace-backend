package com.anush.hamrospace.listing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.anush.hamrospace.common.mapper.MapStructMapperHelper;
import com.anush.hamrospace.common.storage.util.DefaultImageMapper;
import com.anush.hamrospace.listing.dto.request.ListingCreateRequest;
import com.anush.hamrospace.listing.dto.request.ListingUpdateRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import org.mapstruct.MappingTarget;
import com.anush.hamrospace.listing.entity.Listing;

@Mapper(componentModel = "spring",uses = {DefaultImageMapper.class,MapStructMapperHelper.class},nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE)
public interface ListingMapper {


    @Mapping(target = "coverImageUrl", expression = "java(list.getCoverImageUrl())")
    ListingSummaryResponse toSummaryResponse(Listing list);

   @Mapping(target = "host.userId",source = "host.id")
   @Mapping(target = "host.fullName",source="host.profile.fullName")
   @Mapping(target = "host.profileImageUrl",source = "host.profile.profileImageUrl")
    ListingDetailedResponse toDetailedResponse(Listing list);


    Listing toEntity(ListingCreateRequest request);

    void updateListing(ListingUpdateRequest request,@MappingTarget Listing listing);
    
}
