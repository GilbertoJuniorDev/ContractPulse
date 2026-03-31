package com.contractpulse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para sincronizar dados do usuário após login via Supabase Auth.
 * O ID do usuário vem do JWT (sub claim), não do request body.
 */
public record SyncUserRequest(

        @NotBlank(message = "Nome completo é obrigatório")
        String fullName,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        String avatarUrl
) {
}
