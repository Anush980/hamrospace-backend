package com.anush.hamrospace.common.dto.response;

public record FieldErrorResponse(
    String field,
    String message
) {}
