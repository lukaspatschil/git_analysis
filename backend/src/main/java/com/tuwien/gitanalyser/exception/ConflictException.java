package com.tuwien.gitanalyser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ConflictException extends ResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public ConflictException(final String message) {
        super(STATUS, message);
    }
}
