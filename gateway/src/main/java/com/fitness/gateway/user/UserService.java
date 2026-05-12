package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient.Builder webClientBuilder;

    public Mono<Boolean> validateUser(String userId) {
        log.info("calling user service for {}", userId);

        return webClientBuilder.build()
                .get()
                .uri("http://user-service/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new RuntimeException("User not found: " + userId));
                    } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Invalid: " + userId));
                    }
                    return Mono.error(new RuntimeException("Unexpected error: " + userId));
                })
                .onErrorResume(e -> {
                    log.error("User validation failed for userId: {}", userId, e);
                    return Mono.just(false);
                });
    }

    public Mono<UserResponse> registerUser(RegisterRequest registerRequest) {
        log.info("calling user registration for {}", registerRequest.getEmail());

        return webClientBuilder.build()
                .post()
                .uri("http://user-service/api/users/register")          // ✅ fixed URI to match service name
                .bodyValue(registerRequest)                              // ✅ removed stray semicolon
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Bad request: " + e.getMessage()));
                    }
                    return Mono.error(new RuntimeException(            // ✅ replaced userId with email
                            "Unexpected error for user: " + registerRequest.getEmail()));
                })
                .onErrorResume(e -> {
                    log.error("User registration failed for email: {}",// ✅ replaced userId with email
                            registerRequest.getEmail(), e);
                    return Mono.empty();                               // ✅ Mono.empty() fits Mono<UserResponse>, not Mono.just(false)
                });
    }
}