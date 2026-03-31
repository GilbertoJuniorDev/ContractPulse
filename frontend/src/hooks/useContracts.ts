'use client'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createContract,
  fetchContractById,
  fetchContractsByOrganization,
  fetchActiveContractsByOrganization,
  fetchContractsByClient,
  updateContract,
  pauseContract,
  resumeContract,
  terminateContract,
} from '@/lib/api/contracts'
import type {
  CreateContractRequest,
  UpdateContractRequest,
} from '@/lib/types/contract'

const CONTRACTS_KEY = ['contracts'] as const

/**
 * Hook para listar todos os contratos de uma organização.
 */
export function useContractsByOrganization(organizationId: string) {
  return useQuery({
    queryKey: [...CONTRACTS_KEY, 'organization', organizationId],
    queryFn: () => fetchContractsByOrganization(organizationId),
    enabled: !!organizationId,
  })
}

/**
 * Hook para listar contratos ativos de uma organização.
 */
export function useActiveContractsByOrganization(organizationId: string) {
  return useQuery({
    queryKey: [...CONTRACTS_KEY, 'organization', organizationId, 'active'],
    queryFn: () => fetchActiveContractsByOrganization(organizationId),
    enabled: !!organizationId,
  })
}

/**
 * Hook para listar contratos de um cliente.
 */
export function useContractsByClient(clientUserId: string) {
  return useQuery({
    queryKey: [...CONTRACTS_KEY, 'client', clientUserId],
    queryFn: () => fetchContractsByClient(clientUserId),
    enabled: !!clientUserId,
  })
}

/**
 * Hook para buscar um contrato pelo ID.
 */
export function useContract(id: string) {
  return useQuery({
    queryKey: [...CONTRACTS_KEY, id],
    queryFn: () => fetchContractById(id),
    enabled: !!id,
  })
}

/**
 * Hook para criar um novo contrato.
 * Invalida o cache da listagem após sucesso.
 */
export function useCreateContract() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CreateContractRequest) => createContract(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CONTRACTS_KEY })
    },
  })
}

/**
 * Hook para atualizar um contrato existente.
 * Invalida cache da listagem e do detalhe após sucesso.
 */
export function useUpdateContract() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateContractRequest }) =>
      updateContract(id, data),
    onSuccess: (_result, variables) => {
      queryClient.invalidateQueries({ queryKey: CONTRACTS_KEY })
      queryClient.invalidateQueries({
        queryKey: [...CONTRACTS_KEY, variables.id],
      })
    },
  })
}

/**
 * Hook para pausar um contrato ativo.
 * Invalida cache da listagem e do detalhe após sucesso.
 */
export function usePauseContract() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => pauseContract(id),
    onSuccess: (_result, id) => {
      queryClient.invalidateQueries({ queryKey: CONTRACTS_KEY })
      queryClient.invalidateQueries({ queryKey: [...CONTRACTS_KEY, id] })
    },
  })
}

/**
 * Hook para reativar um contrato pausado.
 * Invalida cache da listagem e do detalhe após sucesso.
 */
export function useResumeContract() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => resumeContract(id),
    onSuccess: (_result, id) => {
      queryClient.invalidateQueries({ queryKey: CONTRACTS_KEY })
      queryClient.invalidateQueries({ queryKey: [...CONTRACTS_KEY, id] })
    },
  })
}

/**
 * Hook para encerrar um contrato.
 * Invalida cache da listagem e do detalhe após sucesso.
 */
export function useTerminateContract() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => terminateContract(id),
    onSuccess: (_result, id) => {
      queryClient.invalidateQueries({ queryKey: CONTRACTS_KEY })
      queryClient.invalidateQueries({ queryKey: [...CONTRACTS_KEY, id] })
    },
  })
}
