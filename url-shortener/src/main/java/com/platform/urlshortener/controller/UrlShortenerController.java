package com.platform.urlshortener.controller;

import com.platform.urlshortener.dto.CreateUrlRequest;
import com.platform.urlshortener.dto.CreateUrlResponse;
import com.platform.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(
            UrlShortenerService urlShortenerService
    ) {
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping
    public ResponseEntity<CreateUrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request
    ) {

        CreateUrlResponse response =
                urlShortenerService.createShortUrl(request.url());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}