package com.contractpulse.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para o CurrentUserService.
 */
@DisplayName("CurrentUserService")
class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getCurrentUserId")
    class GetCurrentUserId {

        @Test
        @DisplayName("deve retornar UUID do usuário autenticado")
        void shouldReturnUserIdFromSecurityContext() {
            UUID expectedUserId = UUID.randomUUID();
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_AUTHENTICATED"));
            var authentication = new UsernamePasswordAuthenticationToken(
                    expectedUserId, null, authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UUID actualUserId = currentUserService.getCurrentUserId();

            assertThat(actualUserId).isEqualTo(expectedUserId);
        }

        @Test
        @DisplayName("deve lançar exceção quando não há autenticação no contexto")
        void shouldThrowWhenNoAuthentication() {
            SecurityContextHolder.clearContext();

            assertThatThrownBy(() -> currentUserService.getCurrentUserId())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando authentication é null")
        void shouldThrowWhenAuthenticationIsNull() {
            SecurityContextHolder.getContext().setAuthentication(null);

            assertThatThrownBy(() -> currentUserService.getCurrentUserId())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando principal é null")
        void shouldThrowWhenPrincipalIsNull() {
            var authentication = new UsernamePasswordAuthenticationToken(
                    null, null, List.of()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            assertThatThrownBy(() -> currentUserService.getCurrentUserId())
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
