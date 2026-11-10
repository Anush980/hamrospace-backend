package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;

import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;

public class UnauthorizedAccessException extends AppException {
    public UnauthorizedAccessException(ErrorCodes code,String message) {
        super(HttpStatus.UNAUTHORIZED, code, message);
    }
    public UnauthorizedAccessException(String message) {
        super(HttpStatus.UNAUTHORIZED,ErrorCodes.UNAUTHORIZED, message);
    }
    public UnauthorizedAccessException() {
        super(HttpStatus.UNAUTHORIZED,ErrorCodes.UNAUTHORIZED, "You are not authorized to perform this action");
    }
}
