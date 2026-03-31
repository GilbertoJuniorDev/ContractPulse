/**
 * Tipos e interfaces do módulo de contratos.
 * Refletem os DTOs do backend Java.
 */

export type ContractType = 'RETAINER' | 'FIXED_OVERAGE' | 'PROFIT_SHARING'

export type ContractStatus = 'ACTIVE' | 'PAUSED' | 'TERMINATED'

export type ContractCurrency = 'BRL' | 'USD' | 'EUR'

export type RolloverPolicy = 'EXPIRE' | 'ACCUMULATE' | 'PARTIAL'

/**
 * Configuração específica de contrato Retainer (espelho do JSONB do backend).
 */
export interface RetainerConfig {
  monthlyHours: number
  hourlyRate: number
  rolloverPolicy: RolloverPolicy
  alertThreshold: number
  overageAllowed: boolean
  overageRate: number | null
}

/**
 * Resposta do backend para um contrato.
 */
export interface Contract {
  id: string
  organizationId: string
  clientUserId: string
  title: string
  type: ContractType
  currency: ContractCurrency
  billingDay: number
  startDate: string
  endDate: string | null
  status: ContractStatus
  config: Record<string, unknown>
  createdAt: string
  updatedAt: string
}

/**
 * DTO de criação — campos obrigatórios para criar contrato Retainer.
 */
export interface CreateContractRequest {
  organizationId: string
  clientUserId: string
  title: string
  type: ContractType
  currency: ContractCurrency
  billingDay: number
  startDate: string
  endDate: string | null
  retainerConfig: RetainerConfigRequest | null
}

/**
 * Configuração de retainer enviada na criação/atualização.
 */
export interface RetainerConfigRequest {
  monthlyHours: number
  hourlyRate: number
  rolloverPolicy: RolloverPolicy
  alertThreshold: number
  overageAllowed: boolean
  overageRate: number | null
}

/**
 * DTO de atualização — campos opcionais (parcial update).
 */
export interface UpdateContractRequest {
  title?: string | null
  currency?: ContractCurrency | null
  billingDay?: number | null
  endDate?: string | null
  retainerConfig?: RetainerConfigRequest | null
}
