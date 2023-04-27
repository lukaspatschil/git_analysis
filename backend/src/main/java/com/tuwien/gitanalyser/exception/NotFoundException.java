package com.tuwien.gitanalyser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException extends ResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public NotFoundException(final String message) {
        super(STATUS, message);
    }
}
