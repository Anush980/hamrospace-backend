package com.anush.hamrospace.core.exception.base;

import org.springframework.http.HttpStatusCode;

import com.anush.hamrospace.core.exception.ErrorCodes;

import lombok.Getter;

/**
 * Base runtime exception — always carries an HTTP status and a machine-readable
 * code.
 */
@Getter
public class AppException extends RuntimeException {
    private final HttpStatusCode status;
    private final ErrorCodes code;

    public AppException(HttpStatusCode status, ErrorCodes code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

}
