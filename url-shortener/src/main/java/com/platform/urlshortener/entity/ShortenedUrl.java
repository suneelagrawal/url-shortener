package com.platform.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "shortened_urls")
public class ShortenedUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "short_code",
            nullable = false,
            unique = true,
            length = 20
    )
    private String shortCode;

    @Column(
            name = "original_url",
            nullable = false,
            length = 2048
    )
    private String originalUrl;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "click_count",
            nullable = false
    )
    private long clickCount = 0;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    // null expiresAt means no expiration
    @Column(name = "expires_at")
    private Instant expiresAt;    

    protected ShortenedUrl() {
    }

    public ShortenedUrl(
            String shortCode,
            String originalUrl,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getClickCount() {
        return clickCount;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void recordAccess() {
        this.clickCount++;
        this.lastAccessedAt = Instant.now();
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null
                && Instant.now().isAfter(expiresAt);
    }    
}