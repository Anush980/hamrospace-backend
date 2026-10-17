package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;
import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(ErrorCodes code,String message) {
        super(HttpStatus.NOT_FOUND, code, message);
    }

     public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND, "The requested resource could not be found");
    }

}