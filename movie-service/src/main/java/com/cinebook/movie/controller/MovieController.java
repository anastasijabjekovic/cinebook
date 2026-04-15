package com.cinebook.movie.controller;

import com.cinebook.movie.dto.CreateMovieRequest;
import com.cinebook.movie.dto.MovieResponse;
import com.cinebook.movie.dto.MovieWithUserResponse;
import com.cinebook.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /**
     * Combines movie details with user profile in a single response.
     * Both lookups execute concurrently via {@code Mono.zip}.
     *
     * <p>Spring MVC handles {@code Mono<ResponseEntity<T>>} return types
     * through {@code ReactiveAdapterRegistry} (spring-webflux on classpath).
     *
     * @param id     movie ID
     * @param userId user ID to fetch from user-service
     */
    @GetMapping("/{id}/with-user")
    public Mono<ResponseEntity<MovieWithUserResponse>> getMovieWithUser(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return movieService.getMovieWithUser(id, userId)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }
}
