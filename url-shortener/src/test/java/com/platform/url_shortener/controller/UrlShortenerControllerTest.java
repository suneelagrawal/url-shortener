package com.platform.urlshortener.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateShortUrl() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "https://github.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://github.com"));
    }

    @Test
    void shouldRejectInvalidUrl() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "ftp://test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("URL must start with http:// or https://"));
    }
}