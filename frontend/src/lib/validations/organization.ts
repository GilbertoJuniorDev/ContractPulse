import { z } from 'zod'

/**
 * Schema de validação para criação de organização.
 * Fonte única de verdade para os tipos e validações do formulário.
 */
export const createOrganizationSchema = z.object({
  name: z
    .string()
    .min(2, 'O nome deve ter pelo menos 2 caracteres')
    .max(255, 'O nome deve ter no máximo 255 caracteres'),
})

/**
 * Schema de validação para atualização de organização.
 */
export const updateOrganizationSchema = z.object({
  name: z
    .string()
    .min(2, 'O nome deve ter pelo menos 2 caracteres')
    .max(255, 'O nome deve ter no máximo 255 caracteres'),
})

export type CreateOrganizationFormData = z.infer<typeof createOrganizationSchema>
export type UpdateOrganizationFormData = z.infer<typeof updateOrganizationSchema>
