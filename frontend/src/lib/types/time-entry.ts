/**
 * Tipos e interfaces do módulo de lançamentos de horas.
 */

export type TimeEntryStatus = 'PENDING' | 'APPROVED' | 'DISPUTED'

export interface TimeEntry {
  id: string
  contractId: string
  userId: string
  description: string
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
