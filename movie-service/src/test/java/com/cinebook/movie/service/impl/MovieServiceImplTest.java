package com.cinebook.movie.service.impl;

import com.cinebook.movie.client.UserClient;
import com.cinebook.movie.dto.CreateMovieRequest;
import com.cinebook.movie.dto.MovieResponse;
import com.cinebook.movie.entity.Movie;
import com.cinebook.movie.exception.MovieNotFoundException;
import com.cinebook.movie.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieServiceImpl — unit tests")
class MovieServiceImplTest {

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private static final Long          MOVIE_ID  = 1L;
    private static final String        TITLE     = "Inception";
    private static final String        GENRE     = "Sci-Fi";
    private static final int           DURATION  = 148;
    private static final LocalDateTime SHOWTIME  = LocalDateTime.of(2025, 6, 20, 19, 30);
    private static final LocalDateTime CREATED   = LocalDateTime.of(2024, 1, 1, 8, 0);

    // ── Collaborators (mocked) ────────────────────────────────────────────────

    @Mock
    private MovieRepository movieRepository;

    /**
     * Mocked because MovieServiceImpl requires it via @RequiredArgsConstructor.
     * Not exercised in these tests — reactive Mono.zip tests belong in a
     * separate integration/reactor test class.
     */
    @Mock
    private UserClient userClient;

    // ── Subject under test ────────────────────────────────────────────────────

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(MOVIE_ID)
                .title(TITLE)
                .genre(GENRE)
                .duration(DURATION)
                .showtime(SHOWTIME)
                .createdAt(CREATED)
                .build();
    }

    // ── getMovieById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMovieById — when movie exists — returns correctly mapped response")
    void getMovieById_whenMovieExists_returnsMappedMovieResponse() {
        // given
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(testMovie));

        // when
        MovieResponse response = movieService.getMovieById(MOVIE_ID);

        // then — all fields map correctly from entity to DTO
        assertThat(response.id()).isEqualTo(MOVIE_ID);
        assertThat(response.title()).isEqualTo(TITLE);
        assertThat(response.genre()).isEqualTo(GENRE);
        assertThat(response.duration()).isEqualTo(DURATION);
        assertThat(response.showtime()).isEqualTo(SHOWTIME);
        assertThat(response.createdAt()).isEqualTo(CREATED);
        verify(movieRepository).findById(MOVIE_ID);
    }

    @Test
    @DisplayName("getMovieById — when movie does not exist — throws MovieNotFoundException")
    void getMovieById_whenMovieNotFound_throwsMovieNotFoundException() {
        // given
        Long missingId = 999L;
        when(movieRepository.findById(missingId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> movieService.getMovieById(missingId))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining(String.valueOf(missingId));

        verify(movieRepository).findById(missingId);
    }

    // ── createMovie ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createMovie — persists entity with correct fields and returns response")
    void createMovie_persistsEntityWithCorrectFieldsAndReturnsResponse() {
        // given
        LocalDateTime futureShowtime = LocalDateTime.now().plusDays(7);
        CreateMovieRequest request = new CreateMovieRequest("Interstellar", "Drama", 169, futureShowtime);

        Movie savedMovie = Movie.builder()
                .id(2L).title("Interstellar").genre("Drama")
                .duration(169).showtime(futureShowtime).createdAt(CREATED).build();
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // when
        MovieResponse response = movieService.createMovie(request);

        // then
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.title()).isEqualTo("Interstellar");
        assertThat(response.duration()).isEqualTo(169);

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(captor.capture());
        Movie persisted = captor.getValue();
        assertThat(persisted.getTitle()).isEqualTo("Interstellar");
        assertThat(persisted.getGenre()).isEqualTo("Drama");
        assertThat(persisted.getDuration()).isEqualTo(169);
        assertThat(persisted.getShowtime()).isEqualTo(futureShowtime);
    }

    // ── getAllMovies ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllMovies — returns all movies mapped to response DTOs")
    void getAllMovies_returnsMappedResponseList() {
        // given
        Movie second = Movie.builder()
                .id(2L).title("The Matrix").genre("Action")
                .duration(136).showtime(SHOWTIME.plusDays(1)).createdAt(CREATED).build();
        when(movieRepository.findAll()).thenReturn(List.of(testMovie, second));

        // when
        List<MovieResponse> responses = movieService.getAllMovies();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(MovieResponse::title)
                .containsExactly(TITLE, "The Matrix");
        verify(movieRepository).findAll();
    }
}
