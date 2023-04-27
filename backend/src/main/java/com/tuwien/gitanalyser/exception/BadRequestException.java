package com.tuwien.gitanalyser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BadRequestException extends ResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public BadRequestException() {
        super(STATUS);
    }

    public BadRequestException(final String message) {
        super(STATUS, message);
    }
}
