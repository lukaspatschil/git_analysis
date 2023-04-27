package com.tuwien.gitanalyser.exception;

import org.gitlab4j.api.GitLabApiException;

public class TryRefreshException extends Exception {
    public TryRefreshException(final GitLabApiException e) {
        super(e);
    }
}
