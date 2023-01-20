package com.tuwien.gitanalyser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String message;

    public AuthenticationException(final String message) {
        this.message = message;
    }

    public AuthenticationException(final Exception e) {
        this.message = e.getMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }

}
