package com.tuwien.gitanalyser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends ResponseStatusException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public AuthenticationException(final String message) {
        super(STATUS, message);
    }
}
