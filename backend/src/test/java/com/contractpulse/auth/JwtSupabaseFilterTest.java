package com.contractpulse.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Testes unitários para o JwtSupabaseFilter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtSupabaseFilter")
class JwtSupabaseFilterTest {

    private static final String TEST_JWT_SECRET = "super-secret-key-for-testing-purposes-only-32bytes!";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(
            TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8)
    );

    private JwtSupabaseFilter jwtSupabaseFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtSupabaseFilter = new JwtSupabaseFilter(TEST_JWT_SECRET);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String buildValidJwt(UUID userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .signWith(SIGNING_KEY)
                .compact();
    }

    private String buildExpiredJwt(UUID userId) {
        Instant past = Instant.now().minus(2, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", "authenticated")
                .issuedAt(Date.from(past.minus(1, ChronoUnit.HOURS)))
                .expiration(Date.from(past))
                .signWith(SIGNING_KEY)
                .compact();
    }

    @Nested
    @DisplayName("JWT válido")
    class ValidJwt {

        @Test
        @DisplayName("deve autenticar usuário com JWT válido e role authenticated")
        void shouldAuthenticateWithValidJwt() throws ServletException, IOException {
            UUID userId = UUID.randomUUID();
            String token = buildValidJwt(userId, "authenticated");
            request.addHeader("Authorization", "Bearer " + token);

            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo(userId);
            assertThat(authentication.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHENTICATED"));
        }

        @Test
        @DisplayName("deve converter role para uppercase na authority")
        void shouldConvertRoleToUppercase() throws ServletException, IOException {
            UUID userId = UUID.randomUUID();
            String token = buildValidJwt(userId, "service_role");
            request.addHeader("Authorization", "Bearer " + token);

            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE_ROLE"));
        }

        @Test
        @DisplayName("deve usar AUTHENTICATED como role padrão quando claim é null")
        void shouldDefaultToAuthenticatedRole() throws ServletException, IOException {
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            String token = Jwts.builder()
                    .subject(userId.toString())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                    .signWith(SIGNING_KEY)
                    .compact();
            request.addHeader("Authorization", "Bearer " + token);

            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHENTICATED"));
        }
    }

    @Nested
    @DisplayName("JWT inválido ou ausente")
    class InvalidJwt {

        @Test
        @DisplayName("deve continuar filtro sem autenticação quando não há header Authorization")
        void shouldContinueWithoutAuthWhenNoHeader() throws ServletException, IOException {
            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve continuar filtro sem autenticação quando header não começa com Bearer")
        void shouldContinueWithoutAuthWhenNotBearerToken() throws ServletException, IOException {
            request.addHeader("Authorization", "Basic abc123");

            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve continuar filtro sem autenticação quando JWT está expirado")
        void shouldContinueWithoutAuthWhenJwtExpired() throws ServletException, IOException {
            UUID userId = UUID.randomUUID();
            String token = buildExpiredJwt(userId);
            request.addHeader("Authorization", "Bearer " + token);

            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve continuar filtro sem autenticação quando JWT tem assinatura inválida")
        void shouldContinueWithoutAuthWhenInvalidSignature() throws ServletException, IOException {
            SecretKey wrongKey = Keys.hmacShaKeyFor(
                    "wrong-secret-key-for-testing-purposes-only-32bytes!".getBytes(StandardCharsets.UTF_8)
            );
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            String token = Jwts.builder()
                    .subject(userId.toString())
                    .claim("role", "authenticated")
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                    .signWith(wrongKey)
                    .compact();
            request.addHeader("Authorization", "Bearer " + token);

            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("deve continuar filtro sem autenticação quando JWT é malformado")
        void shouldContinueWithoutAuthWhenMalformedJwt() throws ServletException, IOException {
            request.addHeader("Authorization", "Bearer not.a.valid.jwt.token");

            jwtSupabaseFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("signingKey não configurada")
    class NoSigningKey {

        @Test
        @DisplayName("deve continuar filtro sem validar quando jwt-secret está vazio")
        void shouldSkipValidationWhenSecretIsEmpty() throws ServletException, IOException {
            JwtSupabaseFilter filterWithoutKey = new JwtSupabaseFilter("");

            UUID userId = UUID.randomUUID();
            String token = buildValidJwt(userId, "authenticated");
            request.addHeader("Authorization", "Bearer " + token);

            filterWithoutKey.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
