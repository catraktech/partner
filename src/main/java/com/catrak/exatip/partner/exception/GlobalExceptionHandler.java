package com.catrak.exatip.partner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.catrak.exatip.commonlib.dto.JsonResponseDTO;
import com.google.gson.Gson;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PartnerException.class)
    public ResponseEntity<Object> handleCustomerException(PartnerException ex) {
        Gson g = new Gson();
        String exception = g.toJson(new JsonResponseDTO<>(ex.getMessage(), false, JsonResponseDTO.BAD_REQUEST));
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex) {
        Gson g = new Gson();
        String exception = g
                .toJson(new JsonResponseDTO<>(ex.getMessage(), false, JsonResponseDTO.INTERNAL_SERVER_ERROR));
        return new ResponseEntity<>(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
