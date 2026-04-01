/**
 * Tipos e interfaces do módulo de lançamentos de horas.
 *
 * Ciclo de vida: DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 */

export type TimeEntryStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'DISPUTED'
  | 'INVOICED'

export interface TimeEntry {
  id: string
  contractId: string
  userId: string
  description: string
  aiSummary: string | null
  hours: number
  entryDate: string
  status: TimeEntryStatus
  reviewerId: string | null
  reviewedAt: string | null
  disputeReason: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateTimeEntryRequest {
  contractId: string
  description: string
  hours: number
  entryDate: string
}

export interface ReviewTimeEntryRequest {
  disputeReason: string
}

/**
 * Métricas do dashboard do cliente para um contrato.
 */
export interface ClientDashboard {
  contractId: string
  contractTitle: string
  monthlyHours: number
  approvedHoursThisPeriod: number
  remainingHours: number
  burnRatePercentage: number
  pendingEntries: number
  approvedEntries: number
  disputedEntries: number
}
