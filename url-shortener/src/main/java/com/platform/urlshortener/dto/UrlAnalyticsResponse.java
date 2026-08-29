package com.platform.urlshortener.dto;

import java.time.Instant;

public record UrlAnalyticsResponse(
        String shortCode,
        long clickCount,
        Instant createdAt,
        Instant lastAccessedAt
) {
}