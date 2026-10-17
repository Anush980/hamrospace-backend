package com.anush.hamrospace.common.dto.response;

import java.time.Instant;
import java.util.List;

import com.anush.hamrospace.core.exception.ErrorCodes;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final List<FieldErrorResponse> errors;
    private final Instant timeStamp;
    private final String code;

    private ApiResponse(boolean success, String message, T data, List<FieldErrorResponse> errors, String code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.code = code;
        this.timeStamp = Instant.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, null, null);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null, null, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> fail(String message, ErrorCodes code) {
        return new ApiResponse<>(false, message, null, null, code != null ? code.name() : null);
    }

    public static <T> ApiResponse<T> fail(String message, List<FieldErrorResponse> errors, ErrorCodes code) {
        return new ApiResponse<>(false, message, null, errors, code != null ? code.name() : null);
    }
}
