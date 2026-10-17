package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;
import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;

public class ConflictException extends AppException {

    public ConflictException(ErrorCodes code,String message) {
        super(HttpStatus.CONFLICT, code, message);
    }

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, ErrorCodes.CONFLICT_ERROR, message);
    }

    public ConflictException() {
        super(HttpStatus.CONFLICT, ErrorCodes.CONFLICT_ERROR, "The requested operation conflicts with an existing resource");
    }
}