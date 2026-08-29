package com.platform.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class DuplicateAliasException extends RuntimeException {

    public DuplicateAliasException(String customAlias) {
        super("Custom alias already exists: " + customAlias);
    }
}