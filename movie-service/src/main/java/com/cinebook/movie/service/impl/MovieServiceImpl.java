package com.cinebook.movie.service.impl;

import com.cinebook.movie.client.UserClient;
import com.cinebook.movie.dto.CreateMovieRequest;
import com.cinebook.movie.dto.MovieResponse;
import com.cinebook.movie.dto.MovieWithUserResponse;
import com.cinebook.movie.entity.Movie;
import com.cinebook.movie.exception.MovieNotFoundException;
import com.cinebook.movie.repository.MovieRepository;
import com.cinebook.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final UserClient       userClient;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "movies")
    public List<MovieResponse> getAllMovies() {
        log.debug("Fetching all movies from DB (cache miss)");
        return movieRepository.findAll()
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "movie", key = "#id")
    public MovieResponse getMovieById(Long id) {
        log.debug("Fetching movie id={} from DB (cache miss)", id);
        return movieRepository.findById(id)
                .map(MovieResponse::from)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "movies", allEntries = true),
            @CacheEvict(cacheNames = "movie",  allEntries = true)
    })
    public MovieResponse createMovie(CreateMovieRequest request) {
        log.debug("Creating movie: title={}", request.title());

        Movie movie = Movie.builder()
                .title(request.title())
                .genre(request.genre())
                .duration(request.duration())
                .showtime(request.showtime())
                .build();

        Movie saved = movieRepository.save(movie);
        log.info("Created movie id={} title={}", saved.getId(), saved.getTitle());
        return MovieResponse.from(saved);
    }

    @Override
    public Mono<MovieWithUserResponse> getMovieWithUser(Long movieId, Long userId) {
        /*
         * Wrap the blocking DB/cache call in boundedElastic so it doesn't
         * block the event-loop thread used by the reactive pipeline.
         * Mono.zip subscribes to both publishers concurrently, so the
         * user-service HTTP call and the movie lookup run in parallel.
         */
        Mono<MovieResponse> movieMono = Mono
                .fromCallable(() -> getMovieById(movieId))
                .subscribeOn(Schedulers.boundedElastic());

        Mono<com.cinebook.movie.dto.UserDto> userMono = userClient.getUserById(userId);

        return Mono.zip(movieMono, userMono)
                .map(tuple -> new MovieWithUserResponse(tuple.getT1(), tuple.getT2()))
                .doOnSuccess(r -> log.debug(
                        "Assembled MovieWithUserResponse: movieId={}, userId={}", movieId, userId));
    }
}
