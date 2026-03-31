package com.contractpulse.user;

import com.contractpulse.auth.CurrentUserService;
import com.contractpulse.user.dto.SyncUserRequest;
import com.contractpulse.user.dto.UpdateProfileRequest;
import com.contractpulse.user.dto.UserResponse;
import com.contractpulse.user.exception.UserNotFoundException;
import com.contractpulse.user.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o UserController.
 */
@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.contractpulse\\.auth\\.JwtSupabaseFilter"
        )
)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private CurrentUserService currentUserService;

    private UUID userId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userResponse = new UserResponse(
                userId,
                "John Doe",
                "john@example.com",
                "https://example.com/avatar.jpg",
                UserRole.PROVIDER,
                ZonedDateTime.now()
        );

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/me — deve retornar perfil do usuário autenticado")
    void shouldReturnCurrentUserProfile() throws Exception {
        when(userService.findById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/me — deve retornar 404 quando usuário não existe no banco")
    void shouldReturn404WhenUserNotSynced() throws Exception {
        when(userService.findById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users/sync — deve sincronizar e retornar usuário")
    void shouldSyncUser() throws Exception {
        var request = new SyncUserRequest("John Doe", "john@example.com", "https://example.com/avatar.jpg");

        when(userService.syncUser(eq(userId), any(SyncUserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/sync")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users/sync — deve retornar 400 com nome em branco")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        var request = new SyncUserRequest("", "john@example.com", null);

        mockMvc.perform(post("/api/users/sync")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users/sync — deve retornar 400 com email inválido")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        var request = new SyncUserRequest("John Doe", "not-an-email", null);

        mockMvc.perform(post("/api/users/sync")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/users/sync — deve retornar 400 com email em branco")
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        var request = new SyncUserRequest("John Doe", "", null);

        mockMvc.perform(post("/api/users/sync")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/me — deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/users/sync — deve retornar 401 sem autenticação")
    void shouldReturn401OnSyncWithoutAuth() throws Exception {
        var request = new SyncUserRequest("John Doe", "john@example.com", null);

        mockMvc.perform(post("/api/users/sync")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // === PUT /api/users/me ===

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/me — deve atualizar perfil e retornar 200")
    void shouldUpdateProfileAndReturn200() throws Exception {
        var request = new UpdateProfileRequest("John Updated", "https://example.com/new-avatar.jpg");
        var updatedResponse = new UserResponse(
                userId, "John Updated", "john@example.com",
                "https://example.com/new-avatar.jpg", UserRole.PROVIDER, ZonedDateTime.now()
        );

        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("John Updated")))
                .andExpect(jsonPath("$.avatarUrl", is("https://example.com/new-avatar.jpg")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/me — deve retornar 400 quando nome está em branco")
    void shouldReturn400WhenProfileNameIsBlank() throws Exception {
        var request = new UpdateProfileRequest("", null);

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/users/me — deve retornar 404 quando usuário não existe")
    void shouldReturn404WhenProfileUserNotFound() throws Exception {
        var request = new UpdateProfileRequest("Ghost User", null);

        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                .thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/me — deve retornar 401 sem autenticação")
    void shouldReturn401OnUpdateProfileWithoutAuth() throws Exception {
        var request = new UpdateProfileRequest("John", null);

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
