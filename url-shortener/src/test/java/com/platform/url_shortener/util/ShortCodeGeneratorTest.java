package com.platform.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void shouldGenerateSixCharacterShortCode() {
        String code = generator.generate();

        assertNotNull(code);
        assertEquals(6, code.length());
    }

    @Test
    void shouldGenerateOnlyBase62Characters() {
        String code = generator.generate();

        assertTrue(code.matches("[A-Za-z0-9]{6}"));
    }
}