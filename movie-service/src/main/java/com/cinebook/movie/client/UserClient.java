package com.cinebook.movie.client;

import com.cinebook.movie.dto.UserDto;
import com.cinebook.movie.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserClient {

    private static final String USER_SERVICE_BASE_URL = "http://user-service";

    private final WebClient webClient;

    /**
     * The builder is injected with {@code @LoadBalanced} applied in
     * {@link com.cinebook.movie.config.WebClientConfig}, so
     * {@code http://user-service} is resolved via Eureka.
     */
    public UserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(USER_SERVICE_BASE_URL)
                .build();
    }

    /**
     * Reactively fetches a user by ID from user-service.
     *
     * @param userId the user ID to look up
     * @return {@link Mono} emitting the {@link UserDto}, or an error signal
     *         if the user does not exist (404) or user-service is unavailable (5xx)
     */
    public Mono<UserDto> getUserById(Long userId) {
        log.debug("Calling user-service GET /users/{}", userId);
        return webClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> {
                            log.warn("user-service returned 404 for userId={}", userId);
                            return Mono.error(new UserNotFoundException(userId));
                        }
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.error("user-service 5xx error for userId={}: {}", userId, body);
                            return Mono.error(new RuntimeException(
                                    "user-service unavailable (5xx) for userId=" + userId));
                        })
                )
                .bodyToMono(UserDto.class)
                .doOnSuccess(u -> log.debug("Received user from user-service: id={}", u.id()));
    }
}
