package com.platform.urlshortener.service;

import com.platform.urlshortener.dto.CreateUrlResponse;
import com.platform.urlshortener.dto.UrlAnalyticsResponse;
import com.platform.urlshortener.entity.ShortenedUrl;
import com.platform.urlshortener.exception.DuplicateAliasException;
import com.platform.urlshortener.exception.ShortUrlExpiredException;
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
    void shouldGenerateShortCodeWhenCustomAliasIsNotProvided() {

        when(generator.generate())
                .thenReturn("aB12Cd");

        when(repository.existsByShortCode("aB12Cd"))
                .thenReturn(false);

        CreateUrlResponse response =
                service.createShortUrl(
                        "https://github.com",
                        null,
                        null
                );

        assertEquals("aB12Cd", response.shortCode());
        assertEquals(
                "http://localhost:8080/aB12Cd",
                response.shortUrl()
        );
        assertEquals(
                "https://github.com",
                response.originalUrl()
        );

        verify(generator).generate();
        verify(repository).save(any(ShortenedUrl.class));
    }

    @Test
    void shouldRetryWhenGeneratedShortCodeAlreadyExists() {

        when(generator.generate())
                .thenReturn("ABC123")
                .thenReturn("XYZ789");

        when(repository.existsByShortCode("ABC123"))
                .thenReturn(true);

        when(repository.existsByShortCode("XYZ789"))
                .thenReturn(false);

        CreateUrlResponse response =
                service.createShortUrl(
                        "https://github.com",
                        null,
                        null
                );

        assertEquals("XYZ789", response.shortCode());

        verify(generator, times(2)).generate();
        verify(repository).save(any(ShortenedUrl.class));
    }

    @Test
    void shouldReturnOriginalUrl() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now(),
                        null
                );

        when(repository.findByShortCode("aB12Cd"))
                .thenReturn(Optional.of(shortenedUrl));

        String originalUrl =
                service.getOriginalUrl("aB12Cd");

        assertEquals(
                "https://github.com",
                originalUrl
        );

        verify(repository).save(shortenedUrl);
    }

    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {

        when(repository.findByShortCode("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> service.getOriginalUrl("invalid")
        );

        verify(repository, never())
                .save(any(ShortenedUrl.class));
    }

    @Test
    void shouldIncrementClickCountDuringRedirect() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now(),
                        null
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
                        Instant.now(),
                        null
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

    @Test
    void shouldRedirectWhenUrlHasNotExpired() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now(),
                        Instant.now().plusSeconds(3600)
                );

        when(repository.findByShortCode("aB12Cd"))
                .thenReturn(Optional.of(shortenedUrl));

        String originalUrl =
                service.getOriginalUrl("aB12Cd");

        assertEquals(
                "https://github.com",
                originalUrl
        );

        verify(repository).save(shortenedUrl);
    }

    @Test
    void shouldRedirectWhenExpirationIsNotSpecified() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now(),
                        null
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
    void shouldThrowExceptionWhenUrlHasExpired() {

        ShortenedUrl shortenedUrl =
                new ShortenedUrl(
                        "aB12Cd",
                        "https://github.com",
                        Instant.now().minusSeconds(7200),
                        Instant.now().minusSeconds(3600)
                );

        when(repository.findByShortCode("aB12Cd"))
                .thenReturn(Optional.of(shortenedUrl));

        assertThrows(
                ShortUrlExpiredException.class,
                () -> service.getOriginalUrl("aB12Cd")
        );

        verify(repository, never())
                .save(shortenedUrl);
    }

    @Test
    void shouldCreateShortUrlWithCustomAlias() {

        when(repository.existsByShortCode("Spring"))
                .thenReturn(false);

        CreateUrlResponse response =
                service.createShortUrl(
                        "https://github.com",
                        null,
                        "Spring"
                );

        assertEquals(
                "Spring",
                response.shortCode()
        );

        assertEquals(
                "http://localhost:8080/Spring",
                response.shortUrl()
        );

        assertEquals(
                "https://github.com",
                response.originalUrl()
        );

        verify(repository).save(any(ShortenedUrl.class));
        verify(generator, never()).generate();
    }

    @Test
    void shouldRejectDuplicateCustomAlias() {

        when(repository.existsByShortCode("Spring"))
                .thenReturn(true);

        assertThrows(
                DuplicateAliasException.class,
                () -> service.createShortUrl(
                        "https://github.com",
                        null,
                        "Spring"
                )
        );

        verify(repository, never())
                .save(any(ShortenedUrl.class));

        verify(generator, never())
                .generate();
    }

    @Test
    void shouldTreatCustomAliasesAsCaseSensitive() {

        when(repository.existsByShortCode("Spring"))
                .thenReturn(false);

        when(repository.existsByShortCode("spring"))
                .thenReturn(false);

        CreateUrlResponse firstResponse =
                service.createShortUrl(
                        "https://github.com",
                        null,
                        "Spring"
                );

        CreateUrlResponse secondResponse =
                service.createShortUrl(
                        "https://google.com",
                        null,
                        "spring"
                );

        assertEquals(
                "Spring",
                firstResponse.shortCode()
        );

        assertEquals(
                "spring",
                secondResponse.shortCode()
        );

        assertNotEquals(
                firstResponse.shortCode(),
                secondResponse.shortCode()
        );

        verify(repository)
                .existsByShortCode("Spring");

        verify(repository)
                .existsByShortCode("spring");

        verify(repository, times(2))
                .save(any(ShortenedUrl.class));

        verify(generator, never())
                .generate();
    }
}