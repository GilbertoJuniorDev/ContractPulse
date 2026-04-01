/**
 * Fetch wrappers para endpoints de lançamentos de horas no backend Java.
 *
 * Ciclo: DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 */

import { apiFetch } from './client'
import type {
  TimeEntry,
  CreateTimeEntryRequest,
  ReviewTimeEntryRequest,
  ClientDashboard,
} from '@/lib/types/time-entry'

const BASE_PATH = '/api/time-entries'

/**
 * Cria um novo lançamento de horas (status inicial: DRAFT).
 */
export async function createTimeEntry(
  data: CreateTimeEntryRequest
): Promise<TimeEntry> {
  return apiFetch<TimeEntry>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * Submete um lançamento de horas para aprovação (DRAFT → SUBMITTED).
 */
export async function submitTimeEntry(id: string): Promise<TimeEntry> {
  return apiFetch<TimeEntry>(`${BASE_PATH}/${id}/submit`, {
    method: 'PATCH',
  })
}

/**
 * Lista lançamentos do provider autenticado.
 */
export async function fetchMyTimeEntries(): Promise<TimeEntry[]> {
  return apiFetch<TimeEntry[]>(`${BASE_PATH}/my`)
}

/**
 * Lista todos os lançamentos de um contrato.
 */
export async function fetchTimeEntriesByContract(
  contractId: string
): Promise<TimeEntry[]> {
  return apiFetch<TimeEntry[]>(`${BASE_PATH}/contract/${contractId}`)
}

/**
 * Lista lançamentos pendentes de aprovação de um contrato.
 */
export async function fetchPendingTimeEntries(
  contractId: string
): Promise<TimeEntry[]> {
  return apiFetch<TimeEntry[]>(`${BASE_PATH}/contract/${contractId}/pending`)
}

/**
 * Aprova um lançamento de horas (PENDING_APPROVAL → APPROVED).
 */
export async function approveTimeEntry(id: string): Promise<TimeEntry> {
  return apiFetch<TimeEntry>(`${BASE_PATH}/${id}/approve`, {
    method: 'PATCH',
  })
}

/**
 * Disputa um lançamento de horas (PENDING_APPROVAL → DISPUTED).
 */
export async function disputeTimeEntry(
  id: string,
  data: ReviewTimeEntryRequest
): Promise<TimeEntry> {
  return apiFetch<TimeEntry>(`${BASE_PATH}/${id}/dispute`, {
    method: 'PATCH',
    body: JSON.stringify(data),
  })
}

/**
 * Aprova em lote todos os lançamentos PENDING_APPROVAL de um contrato.
 */
export async function batchApproveTimeEntries(
  contractId: string
): Promise<TimeEntry[]> {
  return apiFetch<TimeEntry[]>(
    `${BASE_PATH}/contract/${contractId}/approve-all`,
    { method: 'PATCH' }
  )
}

/**
 * Busca métricas do dashboard do cliente para um contrato.
 */
export async function fetchClientDashboard(
  contractId: string
): Promise<ClientDashboard> {
  return apiFetch<ClientDashboard>(`/api/contracts/${contractId}/client-dashboard`)
}

/**
 * Remove um lançamento de horas em DRAFT.
 */
export async function deleteTimeEntry(id: string): Promise<void> {
  await apiFetch<void>(`${BASE_PATH}/${id}`, {
    method: 'DELETE',
  })
}
