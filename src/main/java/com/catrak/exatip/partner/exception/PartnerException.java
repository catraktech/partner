package com.catrak.exatip.partner.exception;

public class PartnerException extends Exception {

    private static final long serialVersionUID = 1L;

    private String message;

    public PartnerException(String message) {
        super();
        this.message = message;
    }

    public PartnerException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return message;
    }

}