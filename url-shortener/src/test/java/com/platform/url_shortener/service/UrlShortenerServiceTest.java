package com.platform.urlshortener.service;

import com.platform.urlshortener.dto.CreateUrlResponse;
import com.platform.urlshortener.dto.UrlAnalyticsResponse;
import com.platform.urlshortener.entity.ShortenedUrl;
import com.platform.urlshortener.exception.ShortUrlNotFoundException;
import com.platform.urlshortener.repository.ShortenedUrlRepository;
import com.platform.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlShortenerServiceTest {

    private ShortenedUrlRepository repository;
    private ShortCodeGenerator generator;
    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        repository = mock(ShortenedUrlRepository.class);
        generator = mock(ShortCodeGenerator.class);

        service = new UrlShortenerService(
                repository,
                generator
        );
    }

    @Test
    void shouldCreateShortUrl() {

        when(generator.generate())
                .thenReturn("aB12Cd");

        when(repository.existsByShortCode("aB12Cd"))
                .thenReturn(false);

        CreateUrlResponse response =
                service.createShortUrl("https://github.com");

        assertEquals("aB12Cd", response.shortCode());

        assertEquals(
                "http://localhost:8080/aB12Cd",
                response.shortUrl()
        );

        assertEquals(
                "https://github.com",
                response.originalUrl()
        );

        verify(repository).save(any(ShortenedUrl.class));
    }

    @Test
    void shouldRetryWhenShortCodeAlreadyExists() {

        when(generator.generate())
                .thenReturn("ABC123")
                .thenReturn("XYZ789");

        when(repository.existsByShortCode("ABC123"))
                .thenReturn(true);

        when(repository.existsByShortCode("XYZ789"))
                .thenReturn(false);

        CreateUrlResponse response =
                service.createShortUrl("https://github.com");

        assertEquals("XYZ789", response.shortCode());

        verify(generator, times(2)).generate();
    }

    @Test
    void shouldReturnOriginalUrl() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now()
                );

        when(repository.findByShortCode("aB12Cd"))
                .thenReturn(Optional.of(shortenedUrl));

        String originalUrl =
                service.getOriginalUrl("aB12Cd");

        assertEquals(
                "https://github.com",
                originalUrl
        );
    }

    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {

        when(repository.findByShortCode("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> service.getOriginalUrl("invalid")
        );
    }

    @Test
    void shouldIncrementClickCountDuringRedirect() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now()
                );

        when(repository.findByShortCode("aB12Cd"))
                .thenReturn(Optional.of(shortenedUrl));

        service.getOriginalUrl("aB12Cd");

        assertEquals(
                1,
                shortenedUrl.getClickCount()
        );

        assertNotNull(
                shortenedUrl.getLastAccessedAt()
        );

        verify(repository).save(shortenedUrl);
    }

    @Test
    void shouldReturnAnalytics() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now()
                );

        shortenedUrl.recordAccess();
        shortenedUrl.recordAccess();

        when(repository.findByShortCode("aB12Cd"))
                .thenReturn(Optional.of(shortenedUrl));

        UrlAnalyticsResponse response =
                service.getAnalytics("aB12Cd");

        assertEquals(
                "aB12Cd",
                response.shortCode()
        );

        assertEquals(
                2,
                response.clickCount()
        );

        assertNotNull(
                response.lastAccessedAt()
        );
    }
}