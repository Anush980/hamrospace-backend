package com.anush.hamrospace.core.exception.http;

import org.springframework.http.HttpStatus;

import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.base.AppException;



public class AccessDeniedException extends AppException {
    public AccessDeniedException(ErrorCodes code,String message) {
        super(HttpStatus.FORBIDDEN, code, message);
    }

    public AccessDeniedException(String message){
       super(HttpStatus.FORBIDDEN,ErrorCodes.ACCESS_DENIED, message);
    }

    public AccessDeniedException() {
        super(HttpStatus.FORBIDDEN, ErrorCodes.ACCESS_DENIED, "You do not have permission to perform this action");
    }
}
