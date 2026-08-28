package com.platform.urlshortener.repository;

import com.platform.urlshortener.entity.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortenedUrlRepository
        extends JpaRepository<ShortenedUrl, Long> {

    Optional<ShortenedUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);
}