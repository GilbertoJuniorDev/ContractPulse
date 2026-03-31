import { z } from 'zod'

/**
 * Schema de validação para atualização de perfil.
 * Fonte única de verdade para tipos e validações do formulário.
 */
export const updateProfileSchema = z.object({
  fullName: z
    .string()
    .min(2, 'O nome deve ter pelo menos 2 caracteres')
    .max(255, 'O nome deve ter no máximo 255 caracteres'),
  avatarUrl: z
    .string()
    .url('URL inválida')
    .max(500, 'A URL deve ter no máximo 500 caracteres')
    .nullable()
    .optional()
    .or(z.literal('')),
})

export type UpdateProfileFormData = z.infer<typeof updateProfileSchema>
