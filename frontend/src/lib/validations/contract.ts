import { z } from 'zod'

/**
 * Schemas de validação Zod para o módulo de contratos.
 * Fonte única de verdade para tipos e validações dos formulários.
 */

const rolloverPolicyValues = ['EXPIRE', 'ACCUMULATE', 'PARTIAL'] as const
const contractCurrencyValues = ['BRL', 'USD', 'EUR'] as const
const contractTypeValues = ['RETAINER', 'FIXED_OVERAGE', 'PROFIT_SHARING'] as const

/**
 * Schema para configuração Retainer.
 */
export const retainerConfigSchema = z.object({
  monthlyHours: z
    .number({ required_error: 'Horas mensais é obrigatório' })
    .int('Horas mensais deve ser inteiro')
    .positive('Horas mensais deve ser positivo'),
  hourlyRate: z
    .number({ required_error: 'Valor por hora é obrigatório' })
    .positive('Valor por hora deve ser positivo'),
  rolloverPolicy: z.enum(rolloverPolicyValues, {
    required_error: 'Política de rollover é obrigatória',
  }),
  alertThreshold: z
    .number()
    .int()
    .min(1, 'Limite de alerta deve ser entre 1 e 100')
    .max(100, 'Limite de alerta deve ser entre 1 e 100')
    .default(80),
  overageAllowed: z.boolean({ required_error: 'Informar se permite excedente' }),
  overageRate: z
    .number()
    .min(0, 'Valor do excedente deve ser positivo ou zero')
    .nullable()
    .default(null),
})

/**
 * Schema para criação de contrato.
 */
export const createContractSchema = z
  .object({
    organizationId: z.string().uuid('ID da organização é obrigatório'),
    clientUserId: z.string().uuid('ID do cliente é obrigatório'),
    title: z
      .string()
      .min(1, 'Título é obrigatório')
      .max(255, 'Título deve ter no máximo 255 caracteres'),
    type: z.enum(contractTypeValues, {
      required_error: 'Tipo de contrato é obrigatório',
    }),
    currency: z.enum(contractCurrencyValues, {
      required_error: 'Moeda é obrigatória',
    }),
    billingDay: z
      .number({ required_error: 'Dia de faturamento é obrigatório' })
      .int('Dia de faturamento deve ser inteiro')
      .min(1, 'Dia de faturamento deve ser entre 1 e 28')
      .max(28, 'Dia de faturamento deve ser entre 1 e 28'),
    startDate: z.string().min(1, 'Data de início é obrigatória'),
    endDate: z.string().nullable().default(null),
    retainerConfig: retainerConfigSchema.nullable().default(null),
  })
  .refine(
    (data) => {
      if (data.type === 'RETAINER') {
        return data.retainerConfig !== null
      }
      return true
    },
    {
      message: 'Configuração do retainer é obrigatória para contratos do tipo Banco de Horas',
      path: ['retainerConfig'],
    }
  )

/**
 * Schema para atualização de contrato (parcial).
 */
export const updateContractSchema = z.object({
  title: z
    .string()
    .max(255, 'Título deve ter no máximo 255 caracteres')
    .nullable()
    .optional(),
  currency: z.enum(contractCurrencyValues).nullable().optional(),
  billingDay: z
    .number()
    .int()
    .min(1, 'Dia de faturamento deve ser entre 1 e 28')
    .max(28, 'Dia de faturamento deve ser entre 1 e 28')
    .nullable()
    .optional(),
  endDate: z.string().nullable().optional(),
  retainerConfig: retainerConfigSchema.nullable().optional(),
})

export type CreateContractFormData = z.infer<typeof createContractSchema>
export type UpdateContractFormData = z.infer<typeof updateContractSchema>
export type RetainerConfigFormData = z.infer<typeof retainerConfigSchema>
