package com.tuwien.gitanalyser.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SecurityAnnotations {
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@gitServiceImpl.repositoryAccessibleByUser(#authentication.getName(), #platformId) == true")
    public @interface UserOwnsRepo { }
}
