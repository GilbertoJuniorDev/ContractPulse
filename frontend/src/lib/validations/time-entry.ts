import { z } from 'zod'

/**
 * Schema para criação de lançamento de horas.
 */
export const createTimeEntrySchema = z.object({
  contractId: z.string().uuid('ID do contrato é obrigatório'),
  description: z
    .string()
    .min(1, 'Descrição é obrigatória')
    .max(500, 'Descrição deve ter no máximo 500 caracteres'),
  hours: z
    .number()
    .positive('Horas devem ser maiores que zero')
    .max(24, 'Horas não podem exceder 24 por dia'),
  entryDate: z.string().min(1, 'Data é obrigatória'),
})

export type CreateTimeEntryFormData = z.infer<typeof createTimeEntrySchema>

/**
 * Schema para disputa de lançamento.
 */
export const disputeTimeEntrySchema = z.object({
  disputeReason: z
    .string()
    .min(1, 'Motivo da disputa é obrigatório')
    .max(500, 'Motivo deve ter no máximo 500 caracteres'),
})

export type DisputeTimeEntryFormData = z.infer<typeof disputeTimeEntrySchema>
