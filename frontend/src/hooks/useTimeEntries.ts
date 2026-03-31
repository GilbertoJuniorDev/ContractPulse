'use client'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createTimeEntry,
  fetchTimeEntriesByContract,
  fetchPendingTimeEntries,
  approveTimeEntry,
  disputeTimeEntry,
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
 * Hook para listar lançamentos pendentes de um contrato.
 */
export function usePendingTimeEntries(contractId: string) {
  return useQuery({
    queryKey: [...TIME_ENTRIES_KEY, 'contract', contractId, 'pending'],
    queryFn: () => fetchPendingTimeEntries(contractId),
    enabled: !!contractId,
  })
}

/**
 * Hook para criar um lançamento de horas.
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
      queryClient.invalidateQueries({ queryKey: [...CLIENT_DASHBOARD_KEY] })
    },
  })
}

/**
 * Hook para aprovar um lançamento de horas.
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
 * Hook para disputar um lançamento de horas.
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
 * Hook para buscar métricas do dashboard do cliente.
 */
export function useClientDashboard(contractId: string) {
  return useQuery({
    queryKey: [...CLIENT_DASHBOARD_KEY, contractId],
    queryFn: () => fetchClientDashboard(contractId),
    enabled: !!contractId,
  })
}
