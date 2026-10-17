package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;
import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;
public class InvalidCredentialsException extends AppException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS, "Invalid email or password");
    }
}