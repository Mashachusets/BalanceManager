package com.example.demo.business.handlers;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConflict(ConstraintViolationException ex, WebRequest request) {
        String bodyOfResponse = "Value has to be greater than 0";
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {MaxUploadSizeExceededException.class})
    public ResponseEntity <Object> handleMaxSizeException(MaxUploadSizeExceededException exc, String bodyOfResponse, HttpHeaders httpHeaders, HttpStatus expectationFailed, WebRequest request) {
        return handleExceptionInternal(exc, "File size has to be less than 2MB", new HttpHeaders(), HttpStatus.EXPECTATION_FAILED, request);
    }
}