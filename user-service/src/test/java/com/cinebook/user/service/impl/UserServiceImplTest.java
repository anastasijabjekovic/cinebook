package com.cinebook.user.service.impl;

import com.cinebook.user.dto.CreateUserRequest;
import com.cinebook.user.dto.UserResponse;
import com.cinebook.user.entity.User;
import com.cinebook.user.exception.EmailAlreadyExistsException;
import com.cinebook.user.exception.UserNotFoundException;
import com.cinebook.user.repository.UserRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl — unit tests")
class UserServiceImplTest {

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private static final Long   USER_ID    = 1L;
    private static final String USER_NAME  = "Ana Petrović";
    private static final String USER_EMAIL = "ana@example.com";

    // ── Collaborators (mocked) ────────────────────────────────────────────────

    @Mock
    private UserRepository userRepository;

    // ── Subject under test ────────────────────────────────────────────────────

    @InjectMocks
    private UserServiceImpl userService;

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserById — when user exists — returns mapped UserResponse")
    void getUserById_whenUserExists_returnsMappedResponse() {
        // given
        User user = buildUser(USER_ID, USER_NAME, USER_EMAIL);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUserById(USER_ID);

        // then
        assertThat(response.id()).isEqualTo(USER_ID);
        assertThat(response.name()).isEqualTo(USER_NAME);
        assertThat(response.email()).isEqualTo(USER_EMAIL);
        assertThat(response.createdAt()).isNotNull();
        verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("getUserById — when user does not exist — throws UserNotFoundException")
    void getUserById_whenUserNotFound_throwsUserNotFoundException() {
        // given
        Long missingId = 99L;
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUserById(missingId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(String.valueOf(missingId));

        verify(userRepository).findById(missingId);
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createUser — when email is not taken — persists user and returns response")
    void createUser_whenEmailNotTaken_persistsAndReturnsResponse() {
        // given
        CreateUserRequest request = new CreateUserRequest(USER_NAME, USER_EMAIL);
        User savedUser = buildUser(USER_ID, USER_NAME, USER_EMAIL);

        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserResponse response = userService.createUser(request);

        // then
        assertThat(response.id()).isEqualTo(USER_ID);
        assertThat(response.name()).isEqualTo(USER_NAME);
        assertThat(response.email()).isEqualTo(USER_EMAIL);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(USER_NAME);
        assertThat(captor.getValue().getEmail()).isEqualTo(USER_EMAIL);
    }

    @Test
    @DisplayName("createUser — when email already exists — throws EmailAlreadyExistsException and never saves")
    void createUser_whenEmailAlreadyExists_throwsAndNeverSaves() {
        // given
        CreateUserRequest request = new CreateUserRequest(USER_NAME, USER_EMAIL);
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(USER_EMAIL);

        verify(userRepository, never()).save(any());
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers — returns list of UserResponses mapped from all users")
    void getAllUsers_returnsAllUsersMappedToResponses() {
        // given
        List<User> users = List.of(
                buildUser(1L, "Ana",   "ana@example.com"),
                buildUser(2L, "Marko", "marko@example.com"),
                buildUser(3L, "Lena",  "lena@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        // when
        List<UserResponse> responses = userService.getAllUsers();

        // then
        assertThat(responses).hasSize(3);
        assertThat(responses).extracting(UserResponse::email)
                .containsExactly("ana@example.com", "marko@example.com", "lena@example.com");
        verify(userRepository).findAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static User buildUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
    }
}
