package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;

import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;
public class BadRequestException extends AppException {
    public BadRequestException(ErrorCodes code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }

        public BadRequestException( String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, message);
    }

    public BadRequestException() {
        super(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, "Invalid request. Please check your inputs and try again");
    }
}
