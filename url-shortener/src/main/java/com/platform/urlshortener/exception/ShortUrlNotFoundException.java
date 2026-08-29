package com.platform.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String shortCode){
        super("Short URL not found::"+shortCode);
    }
}
