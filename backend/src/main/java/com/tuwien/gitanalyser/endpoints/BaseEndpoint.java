package com.tuwien.gitanalyser.endpoints;

import org.springframework.security.core.Authentication;

public abstract class BaseEndpoint {

    protected static long getUserId(final Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
