package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;
import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;



public class AccessTokenExpiredException extends AppException {

    public AccessTokenExpiredException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCodes.TOKEN_EXPIRED, "The access token provided is expired or invalid");
    }
}
