'use client'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createTimeEntry,
  submitTimeEntry,
  deleteTimeEntry,
  fetchMyTimeEntries,
  fetchTimeEntriesByContract,
  fetchPendingTimeEntries,
  approveTimeEntry,
  disputeTimeEntry,
  batchApproveTimeEntries,
  fetchClientDashboard,
} from '@/lib/api/time-entries'
import type {
  CreateTimeEntryRequest,
  ReviewTimeEntryRequest,
} from '@/lib/types/time-entry'

const TIME_ENTRIES_KEY = ['time-entries'] as const
const CLIENT_DASHBOARD_KEY = ['client-dashboard'] as const

/**
 * Hook para listar todos os lançamentos de um contrato.
 */
export function useTimeEntriesByContract(contractId: string) {
  return useQuery({
    queryKey: [...TIME_ENTRIES_KEY, 'contract', contractId],
    queryFn: () => fetchTimeEntriesByContract(contractId),
    enabled: !!contractId,
  })
}

/**
 * Hook para listar lançamentos do provider autenticado.
 */
export function useMyTimeEntries() {
  return useQuery({
    queryKey: [...TIME_ENTRIES_KEY, 'my'],
    queryFn: () => fetchMyTimeEntries(),
  })
}

/**
 * Hook para listar lançamentos pendentes de aprovação de um contrato.
 */
export function usePendingTimeEntries(contractId: string) {
  return useQuery({
    queryKey: [...TIME_ENTRIES_KEY, 'contract', contractId, 'pending'],
    queryFn: () => fetchPendingTimeEntries(contractId),
    enabled: !!contractId,
  })
}

/**
 * Hook para criar um lançamento de horas (DRAFT).
 * Invalida o cache após sucesso.
 */
export function useCreateTimeEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CreateTimeEntryRequest) => createTimeEntry(data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: [...TIME_ENTRIES_KEY, 'contract', variables.contractId],
      })
      queryClient.invalidateQueries({ queryKey: [...TIME_ENTRIES_KEY, 'my'] })
      queryClient.invalidateQueries({ queryKey: [...CLIENT_DASHBOARD_KEY] })
    },
  })
}

/**
 * Hook para submeter um lançamento de horas (DRAFT → SUBMITTED).
 */
export function useSubmitTimeEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => submitTimeEntry(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...TIME_ENTRIES_KEY] })
    },
  })
}

/**
 * Hook para remover um lançamento de horas em DRAFT.
 * Invalida o cache após sucesso.
 */
export function useDeleteTimeEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deleteTimeEntry(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...TIME_ENTRIES_KEY] })
      queryClient.invalidateQueries({ queryKey: [...CLIENT_DASHBOARD_KEY] })
    },
  })
}

/**
 * Hook para aprovar um lançamento de horas (PENDING_APPROVAL → APPROVED).
 */
export function useApproveTimeEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => approveTimeEntry(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...TIME_ENTRIES_KEY] })
      queryClient.invalidateQueries({ queryKey: [...CLIENT_DASHBOARD_KEY] })
    },
  })
}

/**
 * Hook para disputar um lançamento de horas (PENDING_APPROVAL → DISPUTED).
 */
export function useDisputeTimeEntry() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ReviewTimeEntryRequest }) =>
      disputeTimeEntry(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...TIME_ENTRIES_KEY] })
      queryClient.invalidateQueries({ queryKey: [...CLIENT_DASHBOARD_KEY] })
    },
  })
}

/**
 * Hook para aprovar em lote todos os lançamentos pendentes de um contrato.
 * Invalida caches de time-entries e client-dashboard após sucesso.
 */
export function useBatchApproveTimeEntries() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (contractId: string) => batchApproveTimeEntries(contractId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [...TIME_ENTRIES_KEY] })
      queryClient.invalidateQueries({ queryKey: [...CLIENT_DASHBOARD_KEY] })
    },
  })
}

/**
 * Hook para buscar métricas do dashboard do cliente.
 */
export function useClientDashboard(contractId: string) {
  return useQuery({
    queryKey: [...CLIENT_DASHBOARD_KEY, contractId],
    queryFn: () => fetchClientDashboard(contractId),
    enabled: !!contractId,
  })
}
