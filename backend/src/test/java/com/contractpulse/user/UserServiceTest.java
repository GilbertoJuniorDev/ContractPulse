package com.contractpulse.user;

import com.contractpulse.user.dto.SyncUserRequest;
import com.contractpulse.user.dto.UpdateProfileRequest;
import com.contractpulse.user.dto.UserResponse;
import com.contractpulse.user.exception.UserNotFoundException;
import com.contractpulse.user.model.User;
import com.contractpulse.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o UserService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = User.builder()
                .id(userId)
                .fullName("John Doe")
                .email("john@example.com")
                .avatarUrl("https://example.com/avatar.jpg")
                .role(UserRole.CLIENT)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar usuário quando encontrado")
        void shouldReturnUserWhenFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            UserResponse response = userService.findById(userId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(userId);
            assertThat(response.fullName()).isEqualTo("John Doe");
            assertThat(response.email()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("deve lançar UserNotFoundException quando usuário não existe")
        void shouldThrowWhenUserNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(unknownId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    @Nested
    @DisplayName("syncUser")
    class SyncUser {

        @Test
        @DisplayName("deve criar novo usuário quando não existe")
        void shouldCreateNewUserWhenNotExists() {
            var request = new SyncUserRequest("Jane Doe", "jane@example.com", "https://avatar.url");

            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return User.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .role(UserRole.CLIENT)
                        .createdAt(ZonedDateTime.now())
                        .updatedAt(ZonedDateTime.now())
                        .build();
            });

            UserResponse response = userService.syncUser(userId, request);

            assertThat(response).isNotNull();
            assertThat(response.role()).isEqualTo(UserRole.CLIENT);
            assertThat(response.fullName()).isEqualTo("Jane Doe");
            assertThat(response.email()).isEqualTo("jane@example.com");

            verify(userRepository).findById(userId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("deve atualizar perfil quando usuário já existe")
        void shouldUpdateProfileWhenUserExists() {
            var request = new SyncUserRequest("John Updated", "john@example.com", "https://new-avatar.url");

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            UserResponse response = userService.syncUser(userId, request);

            assertThat(response).isNotNull();
            verify(userRepository).findById(userId);
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("deve criar usuário sem avatar quando avatarUrl é null")
        void shouldCreateUserWithoutAvatar() {
            var request = new SyncUserRequest("No Avatar", "noavatar@example.com", null);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return User.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .role(UserRole.CLIENT)
                        .createdAt(ZonedDateTime.now())
                        .updatedAt(ZonedDateTime.now())
                        .build();
            });

            UserResponse response = userService.syncUser(userId, request);

            assertThat(response).isNotNull();
            assertThat(response.fullName()).isEqualTo("No Avatar");
            assertThat(response.avatarUrl()).isNull();
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("deve atualizar nome e avatar do usuário existente")
        void shouldUpdateNameAndAvatar() {
            var request = new UpdateProfileRequest("John Updated", "https://example.com/new-avatar.jpg");

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            UserResponse response = userService.updateProfile(userId, request);

            assertThat(response).isNotNull();
            verify(userRepository).findById(userId);
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("deve atualizar perfil removendo avatar quando avatarUrl é null")
        void shouldUpdateProfileWithNullAvatar() {
            var request = new UpdateProfileRequest("John No Avatar", null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            UserResponse response = userService.updateProfile(userId, request);

            assertThat(response).isNotNull();
            verify(userRepository).findById(userId);
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("deve lançar UserNotFoundException quando usuário não existe")
        void shouldThrowWhenUserNotFound() {
            UUID unknownId = UUID.randomUUID();
            var request = new UpdateProfileRequest("Ghost", null);

            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateProfile(unknownId, request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }
}
