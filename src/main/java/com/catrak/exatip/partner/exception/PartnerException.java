package com.catrak.exatip.partner.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PartnerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String message;

}