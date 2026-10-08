package com.anush.hamrospace.common.mapper;

import org.mapstruct.Condition;
import org.springframework.stereotype.Component;

@Component
public class MapStructMapperHelper {

    @Condition
    public boolean isNotEmpty(String value) {
        return value != null && !value.isBlank();
    }
}