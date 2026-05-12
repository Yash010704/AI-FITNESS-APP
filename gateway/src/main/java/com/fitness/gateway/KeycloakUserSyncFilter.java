package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        RegisterRequest registerRequest = (token != null) ? getUserDetails(token) : null;

        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");

        if (userId == null && registerRequest != null) {
            userId = registerRequest.getKeycloakId();
        }

        if (userId != null && token != null) {
            String finalUserId = userId;
            RegisterRequest finalRegisterRequest = registerRequest;

            return userService.validateUser(finalUserId)
                    .flatMap(exists -> {
                        if (!exists) {
                            if (finalRegisterRequest != null) {
                                log.info("User not found, registering: {}", finalUserId);
                                return userService.registerUser(finalRegisterRequest)
                                        .doOnSuccess(r -> log.info("User registered successfully: {}", finalUserId))
                                        .doOnError(e -> log.error("Failed to register user: {}", finalUserId, e))
                                        .onErrorResume(e -> Mono.empty())
                                        .then();                    // ✅ cast Mono<UserResponse> → Mono<Void>
                            } else {
                                log.warn("User not found and no token details available for: {}", finalUserId);
                                return Mono.<Void>empty();          // ✅ explicit type to avoid flatMap ambiguity
                            }
                        } else {
                            log.info("User already exists, skipping sync: {}", finalUserId);
                            return Mono.<Void>empty();              // ✅ explicit type to avoid flatMap ambiguity
                        }
                    })
                    .then(Mono.defer(() -> {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }

        return chain.filter(exchange);
    }

    @Nullable
    private RegisterRequest getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token.replace("Bearer ", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            String keycloakId = claims.getStringClaim("sub");

            // Try "email" first, fall back to "preferred_username" if Keycloak doesn't include email in token
            String email = claims.getStringClaim("email");
            if (email == null) {
                email = claims.getStringClaim("preferred_username");
                log.warn("'email' claim missing in JWT, falling back to 'preferred_username': {}", email);
            }

            // If we still don't have the required fields, skip registration — don't send a doomed request
            if (email == null || keycloakId == null) {
                log.warn("JWT missing required claims (email/sub), skipping user sync");
                return null;
            }

            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setKeycloakId(keycloakId);
            request.setFirstName(claims.getStringClaim("given_name"));
            request.setLastName(claims.getStringClaim("family_name"));
            request.setPassword("dummy@123123");

            return request;
        } catch (ParseException e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }
}