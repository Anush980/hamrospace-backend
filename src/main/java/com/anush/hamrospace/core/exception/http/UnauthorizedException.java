package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;

import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(ErrorCodes code,String message) {
        super(HttpStatus.UNAUTHORIZED, code, message);
    }
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED,ErrorCodes.UNAUTHORIZED, message);
    }
    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED,ErrorCodes.UNAUTHORIZED, "You are not authorized to perform this action");
    }
}
