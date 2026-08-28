package com.platform.urlshortener.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int CODE_LENGTH = 6;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder shortCode = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            shortCode.append(CHARACTERS.charAt(index));
        }

        return shortCode.toString();
    }
}