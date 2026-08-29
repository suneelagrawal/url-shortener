package com.platform.urlshortener.service;

import com.platform.urlshortener.dto.CreateUrlResponse;
import com.platform.urlshortener.entity.ShortenedUrl;
import com.platform.urlshortener.repository.ShortenedUrlRepository;
import com.platform.urlshortener.util.ShortCodeGenerator;

import com.platform.urlshortener.dto.UrlAnalyticsResponse;

import org.springframework.stereotype.Service;
import com.platform.urlshortener.exception.ShortUrlNotFoundException;

import java.time.Instant;

@Service
public class UrlShortenerService {

    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortenedUrlRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlShortenerService(
            ShortenedUrlRepository repository,
            ShortCodeGenerator shortCodeGenerator
    ) {
        this.repository = repository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    public CreateUrlResponse createShortUrl(String originalUrl) {

        String shortCode = generateUniqueShortCode();

        ShortenedUrl shortenedUrl = new ShortenedUrl(
                shortCode,
                originalUrl,
                Instant.now()
        );

        repository.save(shortenedUrl);

        String shortUrl = "http://localhost:8080/" + shortCode;

        return new CreateUrlResponse(
                shortCode,
                shortUrl,
                originalUrl
        );
    }

    private String generateUniqueShortCode() {

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {

            String shortCode = shortCodeGenerator.generate();

            if (!repository.existsByShortCode(shortCode)) {
                return shortCode;
            }
        }

        throw new IllegalStateException(
                "Unable to generate unique short code"
        );
    }
    public String getOriginalUrl(String shortCode) {

        ShortenedUrl shortenedUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(shortCode)
                );

        shortenedUrl.recordAccess();
        repository.save(shortenedUrl);

        return shortenedUrl.getOriginalUrl();
    }
    public UrlAnalyticsResponse getAnalytics(String shortCode) {

        ShortenedUrl shortenedUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(shortCode)
                );

        return new UrlAnalyticsResponse(
                shortenedUrl.getShortCode(),
                shortenedUrl.getClickCount(),
                shortenedUrl.getCreatedAt(),
                shortenedUrl.getLastAccessedAt()
        );
    }    
}