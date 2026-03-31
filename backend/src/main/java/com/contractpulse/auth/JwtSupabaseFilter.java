package com.contractpulse.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Filtro que valida o JWT emitido pelo Supabase Auth em cada request.
 *
 * Responsabilidades:
 * 1. Extrair o Bearer token do header Authorization
 * 2. Validar a assinatura com HMAC-SHA256 ou ES256/RS256 (JWKS) do Supabase
 * 3. Extrair sub (userId) e role do payload
 * 4. Setar no SecurityContextHolder
 */
@Component
public class JwtSupabaseFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtSupabaseFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey hmacSigningKey;
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @Autowired
    public JwtSupabaseFilter(
            @Value("${supabase.jwt-secret}") String jwtSecret,
            @Value("${supabase.project-url}") String projectUrl) {

        // Configura chave HMAC (para tokens HS256)
        if (StringUtils.hasText(jwtSecret)) {
            this.hmacSigningKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            log.warn("SUPABASE_JWT_SECRET não configurado — validação HMAC desabilitada");
            this.hmacSigningKey = null;
        }

        // Configura JWKS processor para tokens ES256/RS256 (Google OAuth, etc.)
        if (StringUtils.hasText(projectUrl)) {
            try {
                String jwksUrl = projectUrl.replaceAll("/$", "") + "/auth/v1/.well-known/jwks.json";
                JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
                        .create(new URL(jwksUrl))
                        .retrying(true)
                        .build();

                Set<JWSAlgorithm> algorithms = new HashSet<>();
                algorithms.add(JWSAlgorithm.ES256);
                algorithms.add(JWSAlgorithm.RS256);

                JWSKeySelector<SecurityContext> keySelector =
                        new JWSVerificationKeySelector<>(algorithms, jwkSource);

                ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
                processor.setJWSKeySelector(keySelector);
                this.jwtProcessor = processor;

                log.info("JWKS configurado para validação de JWT ES256/RS256: {}", jwksUrl);
            } catch (Exception e) {
                throw new IllegalStateException("Falha ao configurar JWKS do Supabase", e);
            }
        } else {
            log.warn("SUPABASE_URL não configurado — validação JWKS desabilitada");
            this.jwtProcessor = null;
        }
    }

    /**
     * Construtor simplificado para testes unitários (somente HMAC).
     */
    JwtSupabaseFilter(String jwtSecret) {
        if (StringUtils.hasText(jwtSecret)) {
            this.hmacSigningKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            log.warn("SUPABASE_JWT_SECRET não configurado — autenticação JWT desabilitada");
            this.hmacSigningKey = null;
        }
        this.jwtProcessor = null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (hmacSigningKey == null && jwtProcessor == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null) {
            try {
                UUID userId = null;
                String role = null;

                // Tenta validar via HMAC primeiro (HS256)
                if (hmacSigningKey != null) {
                    try {
                        Claims claims = Jwts.parser()
                                .verifyWith(hmacSigningKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

                        userId = UUID.fromString(claims.getSubject());
                        role = claims.get("role", String.class);
                    } catch (Exception hmacEx) {
                        log.debug("JWT não é HMAC-SHA256, tentando JWKS: {}", hmacEx.getMessage());
                    }
                }

                // Se HMAC falhou, tenta via JWKS (ES256/RS256)
                if (userId == null && jwtProcessor != null) {
                    JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
                    userId = UUID.fromString(claimsSet.getSubject());
                    role = (String) claimsSet.getClaim("role");
                }

                if (userId != null) {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + (role != null ? role.toUpperCase() : "AUTHENTICATED"))
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                log.warn("Falha ao validar JWT do Supabase: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
