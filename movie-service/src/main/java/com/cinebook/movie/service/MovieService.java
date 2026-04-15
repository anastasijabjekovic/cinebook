package com.cinebook.movie.service;

import com.cinebook.movie.dto.CreateMovieRequest;
import com.cinebook.movie.dto.MovieResponse;
import com.cinebook.movie.dto.MovieWithUserResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MovieService {

    List<MovieResponse> getAllMovies();

    MovieResponse getMovieById(Long id);

    MovieResponse createMovie(CreateMovieRequest request);

    /**
     * Concurrently fetches movie (from DB/cache) and user (from user-service)
     * and combines them into a single response using {@link Mono#zip}.
     */
    Mono<MovieWithUserResponse> getMovieWithUser(Long movieId, Long userId);
}
