package com.contractpulse.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço que extrai o userId autenticado do SecurityContextHolder.
 * O userId corresponde ao UUID do Supabase Auth (claim "sub" do JWT).
 */
@Service
public class CurrentUserService {

    /**
     * Retorna o UUID do usuário autenticado extraído do JWT.
     *
     * @throws IllegalStateException se não houver usuário autenticado no contexto
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto de segurança");
        }

        return (UUID) authentication.getPrincipal();
    }
}
