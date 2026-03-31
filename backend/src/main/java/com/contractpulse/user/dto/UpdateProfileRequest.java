package com.contractpulse.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para atualização do perfil do usuário autenticado.
 * Apenas nome e avatar são editáveis — e-mail é gerenciado pelo Supabase Auth.
 */
public record UpdateProfileRequest(

        @NotBlank(message = "Nome completo é obrigatório")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
        String fullName,

        @Size(max = 500, message = "A URL do avatar deve ter no máximo 500 caracteres")
        String avatarUrl
) {
}
