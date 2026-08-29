package com.platform.urlshortener.controller;

import com.platform.urlshortener.dto.CreateUrlRequest;
import com.platform.urlshortener.dto.CreateUrlResponse;
import com.platform.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import com.platform.urlshortener.dto.UrlAnalyticsResponse;

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
                        urlShortenerService.createShortUrl(request.url(),request.expiresAt());

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response);
        }

        @GetMapping("/{shortCode}/analytics")
        public ResponseEntity<UrlAnalyticsResponse> getAnalytics(
                @PathVariable String shortCode
        ) {
        return ResponseEntity.ok(
                urlShortenerService.getAnalytics(shortCode)
        );
        }    
}