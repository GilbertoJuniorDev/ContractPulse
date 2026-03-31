/**
 * Fetch wrappers para endpoints de lançamentos de horas no backend Java.
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
 * Cria um novo lançamento de horas.
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
 * Lista todos os lançamentos de um contrato.
 */
export async function fetchTimeEntriesByContract(
  contractId: string
): Promise<TimeEntry[]> {
  return apiFetch<TimeEntry[]>(`${BASE_PATH}/contract/${contractId}`)
}

/**
 * Lista lançamentos pendentes de um contrato.
 */
export async function fetchPendingTimeEntries(
  contractId: string
): Promise<TimeEntry[]> {
  return apiFetch<TimeEntry[]>(`${BASE_PATH}/contract/${contractId}/pending`)
}

/**
 * Aprova um lançamento de horas.
 */
export async function approveTimeEntry(id: string): Promise<TimeEntry> {
  return apiFetch<TimeEntry>(`${BASE_PATH}/${id}/approve`, {
    method: 'PATCH',
  })
}

/**
 * Disputa um lançamento de horas.
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
 * Busca métricas do dashboard do cliente para um contrato.
 */
export async function fetchClientDashboard(
  contractId: string
): Promise<ClientDashboard> {
  return apiFetch<ClientDashboard>(`/api/contracts/${contractId}/client-dashboard`)
}
