package com.tuwien.gitanalyser.exception;

public abstract class GitException extends Exception {
    public GitException(final Throwable cause) {
        super(cause);
    }
}
