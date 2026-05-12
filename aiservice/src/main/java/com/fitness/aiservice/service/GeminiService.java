package com.fitness.aiservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final String geminiApiKey;

    // ✅ @Value on constructor parameters — injected BEFORE constructor body runs
    public GeminiService(
            WebClient.Builder webClientBuilder,
            @Value("${gemini.api.url}") String geminiApiUrl,
            @Value("${gemini.api.key}") String geminiApiKey
    ) {
        this.webClient = webClientBuilder
                .baseUrl(geminiApiUrl.trim())  // ✅ not null anymore
                .build();
        this.geminiApiKey = geminiApiKey;
    }

    public String getRecommendations(String details) {
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", details)
                        })
                }
        );

        try {
            return webClient.post()
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw new RuntimeException("Failed to get response from Gemini API", e);
        }
    }
}