package com.platform.urlshortener.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUrlRequest(

        @NotBlank(message = "URL is required")
        @Pattern(
                regexp = "^(http|https)://.+$",
                message = "URL must start with http:// or https://"
        )
        String url,
        Instant expiresAt

) {
}