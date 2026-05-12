package com.fitness.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {

    private final WebClient.Builder webClientBuilder;

    public boolean validateUser(String userId) {
        log.info("calling user service for {}", userId);
        try {
            Boolean result = webClientBuilder.build()
                    .get()
                    .uri("http://user-service/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {   // catch ALL exceptions, not just WebClientResponseException
            log.error("User validation failed for userId: {}", userId, e);
            return false;
        }
      // ✅ return false if user service call fails
    }
}